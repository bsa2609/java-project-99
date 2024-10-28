package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;

import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    public List<LabelDTO> getAll() {
        return labelRepository.findAll().stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO get(long id) {
        Label label = findById(id);

        return labelMapper.map(label);
    }

    public LabelDTO create(LabelCreateDTO data) {
        Label label = labelMapper.map(data);
        labelRepository.save(label);

        return labelMapper.map(label);
    }

    public LabelDTO update(long id, LabelUpdateDTO data) {
        Label label = findById(id);

        labelMapper.update(data, label);
        labelRepository.save(label);

        return labelMapper.map(label);
    }

    public void delete(long id) {
        labelRepository.deleteById(id);
    }

    public Label findById(long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Label with id %s not found", id)
                ));
    }

    public Label findByName(String name) {
        return labelRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Label with name %s not found", name)
                ));
    }

    public boolean isNameExists(String name) {
        return labelRepository.existsByName(name);
    }
}
