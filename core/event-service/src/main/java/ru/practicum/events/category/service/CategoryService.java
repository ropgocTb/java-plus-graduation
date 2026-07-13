package ru.practicum.events.category.service;

import ru.practicum.events.category.dto.NewCategoryDto;
import ru.practicum.interaction.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto getCategoryById(Long id);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto updateCategory(Long id, NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);
}
