package org.volodymyrzganiaiko.workload_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {
    private String username;
    private String firstName;
    private String lastName;
    private Boolean active;
    private Map<Integer, Map<Month, Integer>> minutesByYearMonth = new HashMap<>();
}
