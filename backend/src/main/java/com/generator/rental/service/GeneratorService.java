package com.generator.rental.service;

import com.generator.rental.dto.GeneratorDTO;
import com.generator.rental.dto.GeneratorDetailDTO;
import com.generator.rental.dto.GeneratorSearchRequest;
import com.generator.rental.entity.Generator;

import java.util.List;

public interface GeneratorService {
    List<GeneratorDTO> searchGenerators(GeneratorSearchRequest request);
    GeneratorDetailDTO getGeneratorDetail(Long id);
    List<GeneratorDTO> getMyGenerators(String merchantUserId);
    void createGenerator(Generator generator, String merchantUserId);
    void updateGenerator(Long id, Generator generator);
    void deleteGenerator(Long id);
}
