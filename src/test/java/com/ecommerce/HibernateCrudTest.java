package com.ecommerce;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HibernateCrudTest {
    @Test
    void categoryAndProductCrud() {
        Long categoryId;
        Long productId;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Category category = new Category("TestCategory", "Test description");
            Product product = new Product("TestProduct", new BigDecimal("19.99"), 5);
            category.addProduct(product);
            session.persist(category);
            tx.commit();
            categoryId = category.getId();
            productId = product.getId();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Category category = session.get(Category.class, categoryId);
            Product product = session.get(Product.class, productId);
            assertEquals("TestCategory", category.getName());
            assertEquals("TestProduct", product.getName());

            Transaction tx = session.beginTransaction();
            product.setPrice(new BigDecimal("24.99"));
            tx.commit();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Product product = session.get(Product.class, productId);
            assertEquals(0, new BigDecimal("24.99").compareTo(product.getPrice()));
            Transaction tx = session.beginTransaction();
            session.remove(session.get(Category.class, categoryId));
            tx.commit();
            assertNull(session.get(Product.class, productId));
        }
    }

    @AfterAll
    static void closeFactory() {
        HibernateUtil.shutdown();
    }
}
