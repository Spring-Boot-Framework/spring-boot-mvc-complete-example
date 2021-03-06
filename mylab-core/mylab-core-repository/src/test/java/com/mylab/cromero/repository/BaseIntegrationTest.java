package com.mylab.cromero.repository;

import com.mylab.cromero.repository.domain.Base;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Iterator;
import java.util.List;


//http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-testing-spring-boot-applications-detecting-config_
// When testing Spring Boot applications this is often not required. Spring Boot’s @*Test annotations will search for your primary configuration automatically whenever you don’t explicitly define one.
//the search algorithm works up from the package that contains the test until it finds a @SpringBootApplication or @SpringBootConfiguration annotated class. As long as you’ve structured your code in a sensible way your main configuration is usually found.

@RunWith(SpringRunner.class)
//@DataJpaTest is searching  @SpringBootConfiguration or @SpringBootApplication
//In this case it will automaticaly find TestRepositoryConfigIt
@DataJpaTest
public class BaseIntegrationTest {

    @Autowired
    BaseDataOnDemand dod;
    @Autowired
    BaseRepository baseRepository;

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testCount() {
        Assert.assertNotNull(
                "Data on demand for 'Base' faailed to initialize correctly",
                dod.getRandomBase());
        long count = baseRepository.count();
        Assert.assertTrue(
                "Counter for 'Base' incorrectly reported there were no entries",
                count > 0);
    }

    @Test
    public void testFind() {
        Base obj = dod.getRandomBase();
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to provide an identifier", id);
        obj = baseRepository.findOne(id);
        Assert.assertNotNull(
                "Find method for 'Base' illegally returned null for id '" + id
                        + "'", obj);
        Assert.assertEquals(
                "Find method for 'Base' returned the incorrect identifier", id,
                obj.getId());
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to initialize correctly",
                dod.getRandomBase());
        long count = baseRepository.count();
        Assert.assertTrue(
                "Too expensive to perform a find all test for 'Base', as there are "
                        + count
                        + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test",
                count < 250);
        List<Base> result = baseRepository.findAll();
        Assert.assertNotNull(
                "Find all method for 'Base' illegally returned null", result);
        Assert.assertTrue(
                "Find all method for 'Base' failed to return any data",
                result.size() > 0);
    }

    @Test
    public void testFindEntries() {
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to initialize correctly",
                dod.getRandomBase());
        long count = baseRepository.count();
        if (count > 20) {
            count = 20;
        }
        int firstResult = 0;
        int maxResults = (int) count;
        List<Base> result = baseRepository.findAll(
                new org.springframework.data.domain.PageRequest(firstResult
                        / maxResults, maxResults)).getContent();
        Assert.assertNotNull(
                "Find entries method for 'Base' illegally returned null",
                result);
        Assert.assertEquals(
                "Find entries method for 'Base' returned an incorrect number of entries",
                count, result.size());
    }

    @Test
    public void testFlush() {
        Base obj = dod.getRandomBase();
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to provide an identifier", id);
        obj = baseRepository.findOne(id);
        Assert.assertNotNull(
                "Find method for 'Base' illegally returned null for id '" + id
                        + "'", obj);
        boolean modified = dod.modifyBase(obj);
        Integer currentVersion = obj.getVersion();
        baseRepository.flush();
        Assert.assertTrue(
                "Version for 'Base' failed to increment on flush directive",
                (currentVersion != null && obj.getVersion() > currentVersion)
                        || !modified);
    }

    @Test
    public void testSaveUpdate() {
        Base obj = dod.getRandomBase();
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to provide an identifier", id);
        obj = baseRepository.findOne(id);
        boolean modified = dod.modifyBase(obj);
        Integer currentVersion = obj.getVersion();
        Base merged = baseRepository.save(obj);
        baseRepository.flush();
        Assert.assertEquals(
                "Identifier of merged object not the same as identifier of original object",
                merged.getId(), id);
        Assert.assertTrue(
                "Version for 'Base' failed to increment on merge and flush directive",
                (currentVersion != null && obj.getVersion() > currentVersion)
                        || !modified);
    }

    @Test
    public void testSave() {
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to initialize correctly",
                dod.getRandomBase());
        Base obj = dod.getNewTransientBase(Integer.MAX_VALUE);
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to provide a new transient entity",
                obj);
        Assert.assertNull("Expected 'Base' identifier to be null", obj.getId());
        try {
            baseRepository.save(obj);
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e
                    .getConstraintViolations().iterator(); iter.hasNext(); ) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName())
                        .append(".").append(cv.getPropertyPath()).append(": ")
                        .append(cv.getMessage()).append(" (invalid value = ")
                        .append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        baseRepository.flush();
        Assert.assertNotNull("Expected 'Base' identifier to no longer be null",
                obj.getId());
    }

    @Test
    public void testDelete() {
        Base obj = dod.getRandomBase();
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull(
                "Data on demand for 'Base' failed to provide an identifier", id);
        obj = baseRepository.findOne(id);
        baseRepository.delete(obj);
        baseRepository.flush();
        Assert.assertNull("Failed to remove 'Base' with identifier '" + id
                + "'", baseRepository.findOne(id));
    }
}
