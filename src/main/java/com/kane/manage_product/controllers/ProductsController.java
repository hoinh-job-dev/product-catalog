package com.kane.manage_product.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.kane.manage_product.models.Product;
import com.kane.manage_product.models.ProductDto;
import com.kane.manage_product.services.ProductsRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping({ "", "/" })
    public String ShowProductList(Model model) {

        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";

    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDto productDto, BindingResult result) {
        // process POST request create new product
        if (productDto.getImageFile() == null || productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
        }
        if (result.hasErrors()) {

            return "products/CreateProduct";
        }

        // save uploaded file to server
        MultipartFile image = productDto.getImageFile();
        Date createAt = new Date();
        String storeFileName = createAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "/static/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Path filePath = uploadPath.resolve(storeFileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // save product to database
        Product product = new Product();
        product.setName(productDto.getName());
        product.setBranch(productDto.getBranch());
        product.setCategory(productDto.getCategory());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setImageFileName(storeFileName);
        product.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));

        repo.save(product);

        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Product product = repo.findById(id).get();
            if (product == null) {
                return "redirect:/products";
            }
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBranch(product.getBranch());
            productDto.setCategory(product.getCategory());
            productDto.setDescription(product.getDescription());
            productDto.setPrice(product.getPrice());

            model.addAttribute("productDto", productDto);

        } catch (Exception e) {
            System.out.println("Edit product Error occurred while fetching product");
            e.printStackTrace();
            return "redirect:/products";
        }

        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result) {
        // process POST request edit -> update product
        try {
            Product product = repo.findById(id).get();
            if (product == null) {
                return "redirect:/products";
            }
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "products/EditProduct";
            }

            String storeFileName = product.getImageFileName();
            if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
                // validate image file
                if (productDto.getImageFile().getSize() > 10 * 1024 * 1024) {
                    result.addError(
                            new FieldError("productDto", "imageFile", "The image file size must be less than 10MB"));
                    return "products/EditProduct";
                }
                // delete old image file
                String uploadDir = "static/images/";
                Path oldImageFileName = Paths.get(uploadDir + product.getImageFileName());
                try {
                    if (Files.exists(oldImageFileName)) {
                        Files.deleteIfExists(oldImageFileName);
                    }
                } catch (Exception e) {
                    System.out.println("Delete old image file error");
                    e.printStackTrace();
                    return "products/EditProduct";
                }

                // save uploaded new file to server
                MultipartFile image = productDto.getImageFile();
                Date createAt = new Date();
                storeFileName = createAt.getTime() + "_" + image.getOriginalFilename();
                try (InputStream inputStream = image.getInputStream()) {
                    // save file to server
                    // ignore processing file name to avoid conflict with existing file
                    // Files.copy(inputStream, Paths.get(uploadDir + storeFileName),
                    // StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storeFileName);
            }

            // update product to database
            product.setName(productDto.getName());
            product.setBranch(productDto.getBranch());
            product.setCategory(productDto.getCategory());
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());
            product.setImageFileName(storeFileName);

            repo.save(product);

        } catch (Exception e) {
            // handle exception
            System.out.println("Update product Error occurred while fetching product");
            e.printStackTrace();
            return "products/EditProduct";
        }

        return "redirect:/products";
    }

}
