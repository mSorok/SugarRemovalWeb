package de.unijena.cheminf.sugarremovalweb;

import de.unijena.cheminf.sugarremovalweb.storage.StorageProperties;
import de.unijena.cheminf.sugarremovalweb.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class SugarRemovalWebApplication {
    static String IMAGE_DIR;


    public static void main(String[] args) {
        IMAGE_DIR = "./molimg/";
        SpringApplication.run(SugarRemovalWebApplication.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }

}
