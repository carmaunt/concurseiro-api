package br.com.concurseiro.api.infra.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String firebaseCredentialsPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            if (firebaseCredentialsPath == null || firebaseCredentialsPath.isBlank()) {
                throw new IllegalStateException("A propriedade firebase.credentials.path não foi configurada.");
            }

            try (InputStream serviceAccount = new FileInputStream(firebaseCredentialsPath)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                return FirebaseApp.initializeApp(options);
            }
        }

        return FirebaseApp.getInstance();
    }
}