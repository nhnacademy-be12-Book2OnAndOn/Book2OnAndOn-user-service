package com.example.book2onandonuserservice.global.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookListResponseDto {
    private Long id;
    private String title;
    private String volume;
    private Long priceStandard;
    private Long priceSales;
    private Double rating;
    private String imagePath;
    private LocalDate publisherDate;
    private List<String> contributorNames;
    private List<String> publisherNames;
    private List<String> categoryIds;
    private List<String> tagNames;
}
