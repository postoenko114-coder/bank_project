package com.example.demo.mapper;

import com.example.demo.dto.CardDTO;
import com.example.demo.models.card.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CardMapper {

    CardDTO toDTO(Card card);

    Card toEntity(CardDTO cardDTO);
}
