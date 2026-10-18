package com.miapeve.finance.service;

import com.miapeve.finance.dto.ProgrammeDTO;
import com.miapeve.finance.dto.ProgrammeRequest;
import com.miapeve.finance.entity.Programme;
import com.miapeve.finance.entity.Utilisateur;
import com.miapeve.finance.repository.ProgrammeRepository;
import com.miapeve.finance.repository.SemenceRepository;
import com.miapeve.finance.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SemenceRepository semenceRepository;

    public ProgrammeService(ProgrammeRepository programmeRepository, UtilisateurRepository utilisateurRepository, SemenceRepository semenceRepository) {
        this.programmeRepository = programmeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.semenceRepository = semenceRepository;
    }

    @Transactional(readOnly = true)
    public List<ProgrammeDTO> findAll() {
        return programmeRepository.findAllByOrderByDateProgrammeDesc().stream().map(this::toDTO).toList();
    }

    @Transactional
    public ProgrammeDTO create(ProgrammeRequest request, Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Programme programme = new Programme();
        programme.setTypeProgramme(request.typeProgramme());
        programme.setDateProgramme(request.dateProgramme());
        programme.setHeureDebut(request.heureDebut());
        programme.setHeureFin(request.heureFin());
        programme.setUtilisateur(utilisateur);
        programme.setCreePar(utilisateurId);

        return toDTO(programmeRepository.save(programme));
    }

    @Transactional
    public void delete(Long id) {
        if (!programmeRepository.existsById(id)) {
            throw new IllegalArgumentException("Programme introuvable: " + id);
        }
        if (!semenceRepository.findByProgrammeId(id).isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer : des semences sont rattachées à ce programme");
        }
        programmeRepository.deleteById(id);
    }

    private ProgrammeDTO toDTO(Programme programme) {
        return new ProgrammeDTO(
                programme.getId(),
                programme.getTypeProgramme(),
                programme.getDateProgramme(),
                programme.getHeureDebut(),
                programme.getHeureFin()
        );
    }
}
