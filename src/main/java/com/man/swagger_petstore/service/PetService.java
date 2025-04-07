package com.man.swagger_petstore.service;

import com.man.swagger_petstore.api.spec.model.Category;
import com.man.swagger_petstore.api.spec.model.Pet;
import com.man.swagger_petstore.api.spec.model.Tag;
import com.man.swagger_petstore.dao.PetRepository;
import com.man.swagger_petstore.exceptions.BusinessException;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PetService {

    private static final Logger LOG = LoggerFactory.getLogger(PetRepository.class);

    @Autowired
    private PetRepository petRepository;

    public void insertPet(Pet pet) {
        LOG.info("Entering addPet() class PetService");

        // Check if pet is valid parameters
        validatePetInput(pet);

        petRepository.insertPet(
                pet.getId(),
                pet.getName(),
                pet.getCategory(),
                pet.getPhotoUrls(),
                pet.getTags(),
                pet.getStatus().toString()
        );

        LOG.info("Exiting addPet() class PetService");
    }

    public void updatePet(Pet pet) {
        LOG.info("Entering updatePet() class PetService");

        validatePetInput(pet);

        petRepository.updatePet(
                pet.getId(),
                pet.getName(),
                pet.getCategory(),
                pet.getPhotoUrls(),
                pet.getTags(),
                pet.getStatus().toString()
        );

        LOG.info("Exiting updatePet() class PetService");
    }

    public Pet getPetById(Long petId) {
        LOG.info("Entering getPetById() class PetService");

        Pet pet = petRepository.getPetById(petId);

        LOG.info("Exiting getPetById() class PetService");
        return pet;
    }

    public List<Pet> getPetsByStatus(List<String> status) {
        LOG.warn("Entering getPetsByStatus() class PetService");
        List<Pet> pets = petRepository.getPetByStatusName(status);
        LOG.warn("Exiting getPetsByStatus() class PetService");
        return pets;
    }

    public List<Pet> getPetsByTags(List<String> tagNames) {
        LOG.info("Entering getPetsByTags() class PetService");

        List<Pet> pets = petRepository.getPetByTagName(tagNames);

        LOG.info("Exiting getPetsByTags() class PetService");
        return pets;
    }

    public void deletePet(Long petId) {
        LOG.info("Entering deletePet() class PetService");
        petRepository.deletePetById(petId);
        LOG.info("Exiting deletePet() class PetService");
    }

    private void validatePetInput(Pet pet) {
        LOG.info("Entering validatePetInput() class PetService");
        if (pet.getId() == null
                || pet.getCategory() == null
                || pet.getName() == null
                || pet.getPhotoUrls() == null
                || pet.getTags() == null
                || pet.getStatus() == null) {
            LOG.debug("Throwing Business Error: invalid input");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(), Constants.Error.INVALID_INPUT, "Request body has null attributes");
        }

        Category p_cat = pet.getCategory();
        if (p_cat.getId() == null
                || p_cat.getName() == null) {
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(), Constants.Error.INVALID_INPUT, "Pet Category has null attributes");
        }

        pet.getTags().forEach(t -> {
            if (t.getId() == null
                    || t.getName() == null) {
                throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(), Constants.Error.INVALID_INPUT, "Pet Tags is null");
            }
        });

        LOG.info("Entering validatePetInput() class PetService");
    }

    public void updatePetWithForm(Long petId, String name, String status) {
        LOG.info("Entering updatePetWithForm() class PetService");

        petRepository.updatePetWithForm(petId, name, status);

        LOG.info("Exiting updatePetWithForm() class PetService");
    }
}
