
// Dans la classe FileStorageServiceImpl.java
package com.universite.UniClubs.services.impl;

import com.universite.UniClubs.services.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    // On définit le dossier racine où seront stockées toutes les images uploadées.
    // "user.dir" correspond au répertoire racine de notre projet.
    private final Path fileStorageLocation = Paths.get(System.getProperty("user.dir") + "/uploads");

    public FileStorageServiceImpl() {
        // On s'assure que le dossier d'upload existe. Si non, on le crée.
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage des fichiers.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        // 1. Nettoyer le nom du fichier
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // 2. Vérifier si le nom du fichier est valide
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Le nom du fichier contient une séquence invalide " + originalFileName);
            }

            // 3. Créer un nom de fichier unique pour éviter les conflits
            String fileExtension = "";
            try {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            } catch(Exception e) {
                // Gérer le cas où il n'y a pas d'extension
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // 4. Créer le chemin de destination complet
            Path targetLocation = this.fileStorageLocation.resolve(subDirectory).resolve(uniqueFileName);
            Files.createDirectories(targetLocation.getParent()); // S'assurer que le sous-dossier existe

            // 5. Copier le fichier vers sa destination
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 6. Retourner le chemin d'accès relatif pour l'URL
            return "/uploads/" + subDirectory + "/" + uniqueFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Impossible de stocker le fichier " + originalFileName + ". Veuillez réessayer!", ex);
        }
    }
}