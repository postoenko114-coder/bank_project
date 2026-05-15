package com.example.demo.models.branch;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class WorkingHour {

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    private LocalTime openTime;
    private LocalTime closeTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkingHour that = (WorkingHour) o;
        return day == that.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day);
    }
}
