package br.com.concurseiro.api.infra.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@Component
public class ApiInfoContributor implements InfoContributor {

    private final DataSource dataSource;

    public ApiInfoContributor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void contribute(Info.Builder builder) {
        String dbStatus = "UP";
        String dbProduct = "unknown";
        try (Connection conn = dataSource.getConnection()) {
            dbProduct = conn.getMetaData().getDatabaseProductName()
                    + " " + conn.getMetaData().getDatabaseProductVersion();
        } catch (Exception e) {
            dbStatus = "DOWN";
        }

        builder.withDetail("database", Map.of(
                "status", dbStatus,
                "product", dbProduct
        ));

        builder.withDetail("runtime", Map.of(
                "java", System.getProperty("java.version"),
                "jvm", System.getProperty("java.vm.name")
        ));
    }
}
