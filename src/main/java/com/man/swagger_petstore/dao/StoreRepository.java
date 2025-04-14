package com.man.swagger_petstore.dao;

import com.man.swagger_petstore.api.spec.model.Order;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Repository
public class StoreRepository {

    private static final Logger LOG = LoggerFactory.getLogger(StoreRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Integer> getInventory() throws SQLException {
        LOG.info("Entering getInventory() class StoreRepository");
        Map<String, Integer> inventory = new HashMap<>();
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            conn.setAutoCommit(false);
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.GET_INVENTORY)) {
                // OUT
                stmt.registerOutParameter(Constants.SQL_Query.FIRST_PARAM, Types.REF_CURSOR);

                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(Constants.SQL_Query.FIRST_PARAM)) {
                    while (rs.next()) {
                        inventory.put(rs.getString("pet_status"), rs.getInt("pet_status_count"));
                    }
                }
            }
            conn.commit();
        }
        LOG.info("Exiting getInventory() class StoreRepository");
        return inventory;
    }

    public void addOrder(
            Long order_id,
            Long pet_id,
            Integer qty,
            OffsetDateTime shipDate,
            String status,
            Boolean complete
    ) throws SQLException {
        LOG.info("Entering addOrder() class StoreRepository");
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.ADD_ORDER)) {
                // IN
                stmt.setLong(Constants.SQL_Query.FIRST_PARAM, order_id);
                stmt.setLong(Constants.SQL_Query.SECOND_PARAM, pet_id);
                stmt.setInt(Constants.SQL_Query.THIRD_PARAM, qty);
                stmt.setObject(Constants.SQL_Query.FOURTH_PARAM, shipDate, Types.TIMESTAMP_WITH_TIMEZONE);
                stmt.setObject(Constants.SQL_Query.FIFTH_PARAM, status);
                stmt.setBoolean(Constants.SQL_Query.SIXTH_PARAM, complete);

                stmt.execute();
            }
        }

        LOG.info("Exiting addOrder() class StoreRepository");
    }

    public Order getOrder(Long orderId) throws SQLException {
        Order order = null;
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            conn.setAutoCommit(false);
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.GET_ORDER)) {
                // IN
                stmt.setLong(Constants.SQL_Query.FIRST_PARAM, orderId);

                // OUT
                stmt.registerOutParameter(Constants.SQL_Query.SECOND_PARAM, Types.REF_CURSOR);

                stmt.execute();

                try (ResultSet rs = stmt.getObject(Constants.SQL_Query.SECOND_PARAM, ResultSet.class)) {
                    if (rs.next()) {
                        OrderMapper orderMapper = new OrderMapper();
                        order = orderMapper.mapRow(rs, 1);
                    }
                }
            }
            conn.commit();
        }
        return order;
    }

    public static class OrderMapper implements RowMapper<Order> {

        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setId(rs.getLong("order_id"));
            order.setPetId(rs.getLong("order_pet_id"));
            order.setQuantity(rs.getInt("order_quantity"));
            order.setShipDate(rs.getObject("order_ship_date", OffsetDateTime.class));
            order.setStatus(Order.StatusEnum.fromValue(rs.getString("order_status")));
            order.setComplete(rs.getBoolean("order_complete"));
            return order;
        }
    }
}
