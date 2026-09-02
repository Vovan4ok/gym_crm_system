package org.volodymyrzganiaiko.workload_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workloads")
@CompoundIndex(name = "idx_name", def = "{'firstName': 1, 'lastName': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummary {
        private int year;
        private List<MonthSummary> months = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        private Month month;
        private int summaryDuration;
    }

    @Id
    private String username;
    private String firstName;
    private String lastName;
    private Boolean active;
    private List<YearSummary> years = new ArrayList<>();
}
