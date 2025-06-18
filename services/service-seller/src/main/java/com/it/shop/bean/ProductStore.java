package com.it.shop.bean;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Product_Store")
@Schema(title = "Product Store")
public class ProductStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sku;

    @Column (name = "init_qty")
    private Integer initQty;

    @Column(name = "qty_balance")
    private Integer qtyBalance;

    @Column
    private Double price;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public Long getSku() {
        return sku;
    }

    public void setSku(Long sku) {
        this.sku = sku;
    }

    public Integer getInitQty() {
        return initQty;
    }

    public void setInitQty(Integer initQty) {
        this.initQty = initQty;
    }

    public Integer getQtyBalance() {
        return qtyBalance;
    }

    public void setQtyBalance(Integer qtyBalance) {
        this.qtyBalance = qtyBalance;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
