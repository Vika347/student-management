package ru.itmo.studentmanagement.filtr;
//Хранит параметры фильтрации
public record StudentFilterCriteria(
        Long id,
        String fullName,
        String group,
        Integer isuId,
        Integer dormNumber,
        Integer roomNumber,
        String settlementDate,
        Boolean isForeigner,
        String notes,
        String search,
        String sortBy,
        String sortOrder,
        Integer page,
        Integer size
) {
}