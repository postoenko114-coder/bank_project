package com.alex.bank.mapper;

import com.alex.bank.dto.CardDTO;
import com.alex.bank.models.card.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CardMapper {

    CardDTO toDTO(Card card);

    Card toEntity(CardDTO cardDTO);
}
