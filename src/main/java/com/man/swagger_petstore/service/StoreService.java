package com.man.swagger_petstore.service;

import com.man.swagger_petstore.api.spec.model.Order;
import com.man.swagger_petstore.api.spec.model.Pet;
import com.man.swagger_petstore.dao.PetRepository;
import com.man.swagger_petstore.dao.StoreRepository;
import com.man.swagger_petstore.exceptions.BusinessException;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class StoreService {

    private static final Logger LOG = LoggerFactory.getLogger(StoreService.class);

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PetService petService;

    public Map<String, Integer> getInventory() {
        LOG.info("Entering getInventory() class StoreService");
        Map<String, Integer> inventory;
        try {
            inventory = storeRepository.getInventory();
        } catch (SQLException e) {
            LOG.warn("Something went wrong in data extract {}", e.getMessage());
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    Constants.Error.BAD_QUERY,
                    e.getMessage());
        }

        LOG.info("Exiting getInventory() class StoreService");
        return inventory;
    }

    public Order placeOrder(Order order) {
        LOG.info("Entering placeOrder() class UserService");
        Order res = null;
        try {
            // Checks if pet exists
            Pet p = petService.getPetById(order.getPetId());

            if (p == null) {
                // Pet ID not returning an existing Pet
                LOG.warn("Pet does not exists");
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Pet does not exisits",
                        "Need a valid PetId");
            } else if (order.getStatus().equals(Order.StatusEnum.PLACED)
                    && (p.getStatus().equals(Pet.StatusEnum.PENDING) || p.getStatus().equals(Pet.StatusEnum.SOLD))) {
                // Pet is already pending or sold
                LOG.warn("Pet already pending/sold for another order");
                throw new BusinessException(HttpStatus.CONFLICT.value(),
                        "Pet already pending or sold from another order",
                        "Need to use another valid Pet");
            }

            // Add the valid order
            storeRepository.addOrder(
                    order.getId(),
                    order.getPetId(),
                    order.getQuantity(),
                    order.getShipDate(),
                    order.getStatus().toString(),
                    order.isComplete()
            );

            // Changed the status on the Pet
            switch (order.getStatus()) {
                case Order.StatusEnum.PLACED -> {
                    // If the order status is placed and pet is valid then make pet status pending
                    petService.updatePetWithForm(order.getPetId(), p.getName(), Pet.StatusEnum.PENDING.toString());
                }
                case Order.StatusEnum.APPROVED -> {
                    if (p.getStatus().equals(Pet.StatusEnum.PENDING)) {
                        // If order status is approved,
                        // then set pet Status to sold
                        petService.updatePetWithForm(order.getPetId(), p.getName(), Pet.StatusEnum.SOLD.toString());
                    }
                }
            }

        } catch (SQLException e) {
            LOG.warn("Something went wrong in data extract {}", e.getMessage());
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    Constants.Error.BAD_QUERY,
                    e.getMessage());
        }
        LOG.info("Exiting placeOrder() class UserService");
        return res;
    }

    public Order getOrder(Long orderId) {
        LOG.info("Entering getOrder() class StoreService");
        Order order = null;
        try {
            order = storeRepository.getOrder(orderId);
        } catch (SQLException e) {
            LOG.warn("Something went wrong in data extract {}", e.getMessage());
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    Constants.Error.BAD_QUERY,
                    e.getMessage());
        }
        LOG.info("Exiting getOrder() class StoreService");
        return order;
    }
}
