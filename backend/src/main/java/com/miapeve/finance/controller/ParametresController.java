package com.miapeve.finance.controller;

import com.miapeve.finance.service.ParametresService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/parametres")
public class ParametresController {

    private final ParametresService parametresService;

    public ParametresController(ParametresService parametresService) {
        this.parametresService = parametresService;
    }

    @GetMapping("/backup")
    public ResponseEntity<byte[]> backup() {
        ParametresService.Sauvegarde sauvegarde = parametresService.creerSauvegarde();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(sauvegarde.nomFichier()).build().toString())
                .body(sauvegarde.contenu());
    }

    @PostMapping("/restore")
    public Map<String, String> restore(@RequestParam("fichier") MultipartFile fichier) {
        parametresService.stagerRestauration(fichier);
        return Map.of("message", "Sauvegarde restaurée : redémarrez l'application pour appliquer les changements.");
    }
}
