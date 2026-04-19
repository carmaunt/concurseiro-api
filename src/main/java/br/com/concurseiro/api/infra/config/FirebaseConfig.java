package br.com.concurseiro.api.infra.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String firebaseCredentialsPath;

    @Value("${firebase.credentials.json:}")
    private String firebaseCredentialsJson;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        FirebaseOptions options;

        if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isBlank()) {
            String json = firebaseCredentialsJson.trim();

            if (!json.startsWith("{")) {
                byte[] decoded = Base64.getDecoder().decode(json);
                json = new String(decoded, StandardCharsets.UTF_8);
            }

            try (InputStream serviceAccount = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
            }
        } else if (firebaseCredentialsPath != null && !firebaseCredentialsPath.isBlank()) {
            try (InputStream serviceAccount = new FileInputStream(firebaseCredentialsPath)) {
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
            }
        } else {
            throw new IllegalStateException(
                    "Configure firebase.credentials.json ou firebase.credentials.path"
            );
        }

        return FirebaseApp.initializeApp(options);
    }
}