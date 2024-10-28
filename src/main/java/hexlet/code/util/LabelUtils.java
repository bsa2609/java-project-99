package hexlet.code.util;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.service.LabelService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@RequiredArgsConstructor
public class LabelUtils {
    private final LabelService labelService;

    private final List<String> defaultLabels = List.of("feature", "bug");

    public void createDefaultLabels() {
        for (String name : defaultLabels) {
            if (!labelService.isNameExists(name)) {
                LabelCreateDTO labelCreateDTO = new LabelCreateDTO();
                labelCreateDTO.setName(name);

                labelService.create(labelCreateDTO);
            }
        }
    }
}
