package com.man.swagger_petstore.controller;

import com.man.swagger_petstore.api.spec.handler.PetApi;
import com.man.swagger_petstore.api.spec.model.ModelApiResponse;
import com.man.swagger_petstore.api.spec.model.Pet;
import com.man.swagger_petstore.dao.PetRepository;
import com.man.swagger_petstore.exceptions.BusinessException;
import com.man.swagger_petstore.service.PetService;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
public class PetController implements PetApi {

    private static final Logger LOG = LoggerFactory.getLogger(PetController.class);

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetService petService;

    @Override
    public ResponseEntity<Void> addPet(Pet body) {
        LOG.info("Entering addPet() class PetController");

        if (body == null) {
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(), Constants.Error.INVALID_INPUT, "Request body should not be null");
        }

        petService.insertPet(body);

        LOG.info("Exiting addPet() class PetController");
        return new ResponseEntity<>(HttpStatus.CREATED);
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
        LOG.info("Entering getPetById() class PetController");

        if (petId == null) {
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(), Constants.Error.INVALID_INPUT, "Request body should not be null");
        }

        petService.getPetById(petId);

        LOG.info("Exiting getPetById() class PetController");
        return new ResponseEntity<>(new Pet(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updatePet(Pet body) {
        return null;
    }

    @Override
    public ResponseEntity<Void> updatePetWithForm(Long petId, String name, String status) {
        return null;
    }

    @Override
    public ResponseEntity<ModelApiResponse> uploadFile(Long petId, String additionalMetadata, MultipartFile file) {
        return null;
    }
}
