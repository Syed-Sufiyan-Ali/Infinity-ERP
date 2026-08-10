package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.Product;
import com.nova.factoryerp.models.ProductCategory;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {
    List<Product> findAll() throws SQLException;
    List<Product> search(String keyword, Integer categoryId, String status) throws SQLException;
    Optional<Product> findById(int id) throws SQLException;
    Optional<Product> findByCode(String code) throws SQLException;
    List<ProductCategory> findAllCategories() throws SQLException;
    void save(Product p) throws SQLException;
    void update(Product p) throws SQLException;
    void delete(int id) throws SQLException;
    void updateStock(int id, BigDecimal newStock) throws SQLException;
    void saveCategory(ProductCategory c) throws SQLException;
}
