package com.man.swagger_petstore.controller;

import com.man.swagger_petstore.api.spec.handler.StoreApi;
import com.man.swagger_petstore.api.spec.model.Order;
import com.man.swagger_petstore.exceptions.BusinessException;
import com.man.swagger_petstore.service.StoreService;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
public class StoreController implements StoreApi {

    private static final Logger LOG = LoggerFactory.getLogger(StoreController.class);

    @Autowired
    private StoreService storeService;

    @Override
    public ResponseEntity<Void> deleteOrder(String orderId) {
        return null;
    }

    @Override
    public ResponseEntity<Map<String, Integer>> getInventory() {
        LOG.info("Entering getInventory() class StoreController");
        Map<String, Integer> res = storeService.getInventory();
        LOG.info("Exiting getInventory() class StoreController");
        return ResponseEntity.ok(res);
    }

    @Override
    public ResponseEntity<Order> getOrderById(Long orderId) {
        LOG.info("Entering getOrderById() class StoreController");
        if (orderId == null) {
            LOG.warn("Invalid input");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(),
                    Constants.Error.INVALID_INPUT,
                    "Please enter a valid input");
        }
        Order order = storeService.getOrder(orderId);
        LOG.info("Exiting getOrderById() class StoreController");
        return ResponseEntity.ok(order);
    }

    @Override
    public ResponseEntity<Order> placeOrder(Order body) {
        LOG.info("Entering placeOrder() class StoreController");
        if (body == null) {
            LOG.warn("Invalid input");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(),
                    Constants.Error.INVALID_INPUT,
                    "Please enter a valid input");
        }

        Order order = storeService.placeOrder(body);

        LOG.info("Exiting placeOrder() class StoreController");
        return ResponseEntity.ok(order);
    }
}
