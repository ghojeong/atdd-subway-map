package nextstep.subway.line.application;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.exception.LineNotFoundException;
import nextstep.subway.section.domain.Section;
import nextstep.subway.section.dto.SectionRequest;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {
    private final LineRepository lineRepository;
    private final StationService stationService;

    public LineService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    public LineResponse saveLine(LineRequest lineRequest) {
        Line persistLine = lineRepository.save(toLine(lineRequest));
        return LineResponse.of(persistLine);
    }

    @Transactional(readOnly = true)
    public List<LineResponse> getLines() {
        return lineRepository.findAll()
                .stream()
                .map(LineResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LineResponse getLine(Long id) {
        Line line = getLineFromRepository(id);
        return LineResponse.of(line);
    }

    public LineResponse updateLine(Long id, LineRequest lineRequest) {
        Line line = getLineFromRepository(id);
        line.update(toLine(lineRequest));
        return LineResponse.of(line);
    }

    public void deleteLine(Long id) {
        lineRepository.deleteById(id);
    }

    public void addSectionToLine(Long id, SectionRequest sectionRequest) {
        Line line = getLineFromRepository(id);
        Section section = toSection(sectionRequest, line);
        line.addSection(section);
    }

    public void deleteSectionFromLine(Long lineId, Long stationId) {
        Line line = getLineFromRepository(lineId);
        line.deleteSection(stationId);
    }

    private Line toLine(LineRequest lineRequest) {
        String name = lineRequest.getName();
        String color = lineRequest.getColor();
        Long upStationId = lineRequest.getUpStationId();
        Long downStationId = lineRequest.getDownStationId();
        int distance = lineRequest.getDistance();

        Station upStation = stationService.getStationEntity(upStationId);
        Station downStation = stationService.getStationEntity((downStationId));

        return new Line(name, color, upStation, downStation, distance);
    }

    private Section toSection(SectionRequest sectionRequest, Line line) {
        return new Section(
                line,
                stationService.getStationEntity(sectionRequest.getUpStationId()),
                stationService.getStationEntity(sectionRequest.getDownStationId()),
                sectionRequest.getDistance()
        );
    }

    private Line getLineFromRepository(Long id) {
        return lineRepository.findById(id)
                .orElseThrow(() -> new LineNotFoundException(id));
    }
}
