package com.hl.fambud.service;

import com.hl.fambud.model.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryServiceTest {

    @Test
    public void defaultCategories() {
        for (Category category : CategoryService.defaultCategoriesForNewBudget())
            Assertions.assertTrue(
                CategoryService.DEFAULT_CATEGORY_NAMES.contains(category.getName()));
    }
}
