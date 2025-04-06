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

        validatePetBodyRequest(body);

        petService.insertPet(body);

        LOG.info("Exiting addPet() class PetController");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deletePet(Long petId, Optional<String> apiKey) {
        LOG.info("Entering deletePet() class PetController");

        if (petId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), Constants.Error.INVALID_INPUT, "Please enter a valid id");
        }

        petService.deletePet(petId);

        LOG.info("Exiting deletePet() class PetController");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Pet>> findPetsByStatus(List<String> status) {
        LOG.info("Entering findPetsByStatus() class PetController");

        List<Pet> pets = petService.getPetsByStatus(status);

        LOG.info("Exiting findPetsByStatus() class PetController");
        return new ResponseEntity<>(pets, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Pet>> findPetsByTags(List<String> tags) {
        LOG.info("Entering findPetsByTags() class PetController");
        if (tags == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), Constants.Error.INVALID_INPUT, "Please enter a valid id");
        }

        List<Pet> pets = petService.getPetsByTags(tags);

        LOG.info("Exiting findPetsByTags() class PetController");
        return new ResponseEntity<>(pets, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Pet> getPetById(Long petId) {
        LOG.info("Entering getPetById() class PetController");

        if (petId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), Constants.Error.INVALID_INPUT_ID, "Please enter a valid ID");
        }

        Pet pet = petService.getPetById(petId);

        LOG.info("Exiting getPetById() class PetController");
        return new ResponseEntity<>(pet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updatePet(Pet body) {
        LOG.info("Entering updatePet() class PetController");

        validatePetBodyRequest(body);

        petService.updatePet(body);

        LOG.info("Exiting updatePet() class PetController");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updatePetWithForm(Long petId, String name, String status) {
        return null;
    }

    @Override
    public ResponseEntity<ModelApiResponse> uploadFile(Long petId, String additionalMetadata, MultipartFile file) {
        return null;
    }

    private void validatePetBodyRequest(Pet body) {
        if (body == null) {
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(), Constants.Error.INVALID_INPUT, "Request body should not be null");
        }
    }
}
