package com.man.swagger_petstore.controller;

import com.man.swagger_petstore.api.spec.handler.PetApi;
import com.man.swagger_petstore.api.spec.model.ModelApiResponse;
import com.man.swagger_petstore.api.spec.model.Pet;
import com.man.swagger_petstore.dao.PetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Array;
import java.sql.SQLDataException;
import java.util.List;
import java.util.Optional;

@RestController
public class PetController implements PetApi {

    private static final Logger LOG = LoggerFactory.getLogger(PetController.class);

    @Autowired
    private PetRepository petRepository;

    @Override
    public ResponseEntity<Pet> addPet(Pet body) {
        LOG.info("Entering addPet() class PetController");
        petRepository.insertPet(
                body.getId(),
                body.getName(),
                body.getCategory(),
                body.getPhotoUrls(),
                body.getTags(),
                body.getStatus().toString()
        );
        return null;
    }

    @Override
    public ResponseEntity<Void> deletePet(Long petId, Optional<String> apiKey) {
        return null;
    }

    @Override
    public ResponseEntity<List<Pet>> findPetsByStatus(List<String> status) {
        return null;
    }

    @Override
    public ResponseEntity<List<Pet>> findPetsByTags(List<String> tags) {
        return null;
    }

    @Override
    public ResponseEntity<Pet> getPetById(Long petId) {
        return null;
    }

    @Override
    public ResponseEntity<Pet> updatePet(Pet body) {
        return null;
    }

    @Override
    public ResponseEntity<Pet> updatePetWithForm(Long petId, String name, String status) {
        return null;
    }

    @Override
    public ResponseEntity<ModelApiResponse> uploadFile(Long petId, String additionalMetadata, MultipartFile file) {
        return null;
    }
}
