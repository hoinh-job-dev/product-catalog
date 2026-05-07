package com.kane.manage_product.models;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class ProductDto {

    @NotEmpty(message = "The name is required")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    @NotEmpty(message = "The branch is required")
    private String branch;

    @NotEmpty(message = "The category is required")
    private String category;

    @Min(0)
    private double price;

    @Size(min = 10, message = "The description must be at least 10 characters long")
    @Size(max = 2000, message = "The description must be no more than 2000 characters long")
    private String description;
    
    private MultipartFile imageFile;
}
