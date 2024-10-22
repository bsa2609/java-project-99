package hexlet.code.util;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.service.LabelService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class LabelUtil {
    private final List<String> defaultLabels = List.of("feature", "bug");

    @Autowired
    private LabelService labelService;

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
