package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.dto.ModifyOrganizationalUnitDto;
import at.jku.dke.etutor.task_administration.moodle.CourseCategoryService;
import at.jku.dke.etutor.task_administration.moodle.MoodleConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizationalUnitServiceTest {

    @Test
    void getOrganizationalUnits_withoutFilter() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var service = new OrganizationalUnitService(repo, null);
        var page = Pageable.ofSize(10);

        when(repo.findAll(page)).thenReturn(Page.empty());

        // Act
        service.getOrganizationalUnits(page, null);

        // Assert
        verify(repo).findAll(page);
    }

    @Test
    void getOrganizationalUnits_withFilter() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var service = new OrganizationalUnitService(repo, null);
        var page = Pageable.ofSize(10);
        var filter = "DKE";

        when(repo.findByNameContainsIgnoreCase(filter, page)).thenReturn(Page.empty());

        // Act
        service.getOrganizationalUnits(page, filter);

        // Assert
        verify(repo).findByNameContainsIgnoreCase(filter, page);
    }

    @Test
    void getOrganizationalUnit() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var service = new OrganizationalUnitService(repo, null);
        var id = 2L;

        // Act
        service.getOrganizationalUnit(id);

        // Assert
        verify(repo).findById(id);
    }

    @Test
    void create() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = new CourseCategoryService(new MoodleConfig("", "", false), new ObjectMapper());
        var service = new OrganizationalUnitService(repo, catServ);
        var dto = new ModifyOrganizationalUnitDto("DKE");
        when(repo.save(any(OrganizationalUnit.class))).thenAnswer(x -> x.getArgument(0));

        // Act
        var result = service.create(dto);

        // Assert
        assertEquals(dto.name(), result.getName());
    }

    @Test
    void update() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = new CourseCategoryService(new MoodleConfig("", "", false), new ObjectMapper());
        var service = new OrganizationalUnitService(repo, catServ);

        var dto = new ModifyOrganizationalUnitDto("DKE");
        var ou = new OrganizationalUnit();
        ou.setId(2L);
        ou.setName("SE");

        when(repo.findById(ou.getId())).thenReturn(Optional.of(ou));
        when(repo.save(any(OrganizationalUnit.class))).thenAnswer(x -> x.getArgument(0));

        // Act
        service.update(ou.getId(), dto, null);

        // Assert
        assertEquals(dto.name(), ou.getName());
    }

    @Test
    void update_concurrencyProblem() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = new CourseCategoryService(new MoodleConfig("", "", false), new ObjectMapper());
        var service = new OrganizationalUnitService(repo, catServ);

        var dto = new ModifyOrganizationalUnitDto("DKE");
        var ou = new OrganizationalUnit();
        ou.setId(2L);
        ou.setName("SE");
        ou.setLastModifiedDate(Instant.MAX);

        when(repo.findById(ou.getId())).thenReturn(Optional.of(ou));

        // Act & Assert
        assertThrows(ConcurrencyFailureException.class, () -> service.update(ou.getId(), dto, Instant.MIN));
    }

    @Test
    void update_notFound() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = new CourseCategoryService(new MoodleConfig("", "", false), new ObjectMapper());
        var service = new OrganizationalUnitService(repo, catServ);

        var dto = new ModifyOrganizationalUnitDto("DKE");

        when(repo.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.update(1L, dto, null));
    }

    @Test
    void delete() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = new CourseCategoryService(new MoodleConfig("", "", false), new ObjectMapper());
        var service = new OrganizationalUnitService(repo, catServ);

        // Act
        service.delete(2L);

        // Assert
        verify(repo).deleteById(2L);
    }

    @Test
    void createMoodleObjectsForOrganizationalUnit() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = mock(CourseCategoryService.class, withSettings().useConstructor(new MoodleConfig("", "", true), new ObjectMapper()));
        var service = new OrganizationalUnitService(repo, catServ);

        var ou = new OrganizationalUnit();
        when(catServ.createCourseCategory(any())).thenReturn(CompletableFuture.completedFuture(Optional.of(99)));

        // Act
        service.createMoodleObjectsForOrganizationalUnit(ou);

        // Assert
        assertEquals(99, ou.getMoodleId());
        verify(repo).save(ou);
    }

    @Test
    void createMoodleObjectsForOrganizationalUnit_noResult() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = mock(CourseCategoryService.class, withSettings().useConstructor(new MoodleConfig("", "", true), new ObjectMapper()));
        var service = new OrganizationalUnitService(repo, catServ);

        var ou = new OrganizationalUnit();
        when(catServ.createCourseCategory(any())).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        // Act
        service.createMoodleObjectsForOrganizationalUnit(ou);

        // Assert
        assertNull(ou.getMoodleId());
        verify(repo, never()).save(ou);
    }

    @Test
    void createMoodleObjectsForOrganizationalUnit_moodleIdSet() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = mock(CourseCategoryService.class, withSettings().useConstructor(new MoodleConfig("", "", true), new ObjectMapper()));
        var service = new OrganizationalUnitService(repo, catServ);

        var ou = new OrganizationalUnit();
        ou.setMoodleId(23);

        // Act
        service.createMoodleObjectsForOrganizationalUnit(ou);

        // Assert
        verify(catServ, never()).createCourseCategory(ou);
        verify(repo, never()).save(ou);
    }

    @Test
    void updateMoodleObjectsForOrganizationalUnit() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = mock(CourseCategoryService.class, withSettings().useConstructor(new MoodleConfig("", "", true), new ObjectMapper()));
        var service = new OrganizationalUnitService(repo, catServ);

        var ou = new OrganizationalUnit();

        // Act
        service.updateMoodleObjectsForOrganizationalUnit(ou);

        // Assert
        verify(catServ).updateCourseCategory(ou);
    }

    @Test
    void testCreateMoodleObjectsForOrganizationalUnit() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = mock(CourseCategoryService.class, withSettings().useConstructor(new MoodleConfig("", "", true), new ObjectMapper()));
        var service = new OrganizationalUnitService(repo, catServ);

        var ou = new OrganizationalUnit();
        ou.setId(3L);
        when(repo.findById(ou.getId())).thenReturn(Optional.of(ou));
        when(catServ.createCourseCategory(any())).thenReturn(CompletableFuture.completedFuture(Optional.of(99)));

        // Act
        service.createMoodleObjectsForOrganizationalUnit(3);

        // Assert
        assertEquals(99, ou.getMoodleId());
        verify(repo).save(ou);
    }

    @Test
    void testCreateMoodleObjectsForOrganizationalUnit_invalidId() {
        // Arrange
        var repo = mock(OrganizationalUnitRepository.class);
        var catServ = mock(CourseCategoryService.class, withSettings().useConstructor(new MoodleConfig("", "", true), new ObjectMapper()));
        var service = new OrganizationalUnitService(repo, catServ);

        var ou = new OrganizationalUnit();
        ou.setId(3L);
        when(repo.findById(ou.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.createMoodleObjectsForOrganizationalUnit(3));
    }
}
