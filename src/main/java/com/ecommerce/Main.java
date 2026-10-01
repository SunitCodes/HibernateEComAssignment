package com.ecommerce;

import com.ecommerce.entity.*;
import com.ecommerce.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Long categoryId;
        Long productId;
        Long userId;
        Long orderId;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category category = new Category("Electronics", "Electronic products");
            Product laptop = new Product("Laptop", new BigDecimal("650.00"), 10);
            Product mouse = new Product("Mouse", new BigDecimal("25.00"), 40);
            category.addProduct(laptop);
            category.addProduct(mouse);
            session.persist(category);

            Users user = new Users("customer1", BCrypt.hashpw("password123", BCrypt.gensalt()),
                    "customer1@example.com", Users.Role.CUSTOMER);
            session.persist(user);

            Orders order = new Orders(LocalDateTime.now(), new BigDecimal("700.00"));
            order.addOrderDetail(new OrderDetails(1, new BigDecimal("650.00"), laptop));
            order.addOrderDetail(new OrderDetails(2, new BigDecimal("25.00"), mouse));
            user.addOrder(order);
            session.persist(order);

            tx.commit();
            categoryId = category.getId();
            productId = laptop.getId();
            userId = user.getId();
            orderId = order.getId();
            System.out.println("Saved category, products, user and order.");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Category category = session.get(Category.class, categoryId);
            Product product = session.get(Product.class, productId);
            Users user = session.get(Users.class, userId);

            Transaction tx = session.beginTransaction();
            product.setStockQuantity(product.getStockQuantity() - 1);
            tx.commit();

            List<Product> products = session.createNamedQuery("Product.findByCategory", Product.class)
                    .setParameter("categoryId", categoryId)
                    .getResultList();

            Orders order = session.createQuery(
                    "select distinct o from Orders o join fetch o.user join fetch o.orderDetails d join fetch d.product where o.id = :id",
                    Orders.class)
                    .setParameter("id", orderId)
                    .getSingleResult();

            System.out.println("Category: " + category.getName());
            System.out.println("User: " + user.getUsername());
            System.out.println("Products in category: " + products.size());
            System.out.println("Order: " + order.getId() + ", customer: " + order.getUser().getUsername());
            order.getOrderDetails().forEach(detail ->
                    System.out.println(detail.getProduct().getName() + " x " + detail.getQuantity()));
        }

        HibernateUtil.shutdown();
    }
}
