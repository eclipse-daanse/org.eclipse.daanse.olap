/*
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.olap.check.instance.serializer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.daanse.olap.check.model.check.OlapCheckModel;
import org.eclipse.daanse.olap.check.model.check.OlapCheckPackage;
import org.eclipse.daanse.olap.check.model.provider.CatalogCheckSupplier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@RequireEMF
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourceSetWriteReadTest {

    static int i = 0;
    static Path tempDir;

    @BeforeAll
    public static void beforeAll() throws IOException {
        tempDir = Path.of("./daansetutorials");
        deleteDirectory(tempDir);
        tempDir = Files.createDirectories(tempDir);
    }

    @Test
    @Order(1)
    public void writePopulation(@InjectBundleContext BundleContext bc,
            @InjectService(cardinality = 1, filter = "(" + EMFNamespaces.EMF_MODEL_NAME + "="
                    + "model" + ")") ResourceSet resourceSet,
            @InjectService ServiceAware<CatalogCheckSupplier> mappingSuppiersSA)
            throws SQLException, InterruptedException, IOException {

        try {

            List<ServiceReference<CatalogCheckSupplier>> srs = mappingSuppiersSA.getServiceReferences();

            // Create combined ZIP directory structure
            Path zipDir = Files.createDirectories(tempDir.resolve("cubeserver/tutorial/zip"));
            ZipOutputStream combinedZos = new ZipOutputStream(new FileOutputStream(zipDir.resolve("all-tutorials.zip").toFile()));

            srs.sort((o1, o2) -> {
                Object s1 = o1.getProperty("number");
                Object s2 = o2.getProperty("number");

                String ss1 = s1 == null ? "9999.9.9" : s1.toString();
                String ss2 = s2 == null ? "9999.9.9" : s2.toString();
                return ss1.compareToIgnoreCase(ss2);
            });
            for (ServiceReference<CatalogCheckSupplier> sr : srs) {

                try {
                	CatalogCheckSupplier catalogMappingSupplier = mappingSuppiersSA.getService(sr);

                    serializeCatalog(resourceSet, catalogMappingSupplier, sr.getProperties(), combinedZos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Close combined ZIP
            combinedZos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void serializeCatalog(ResourceSet resourceSet,
    		CatalogCheckSupplier catalogMappingSupplier, Dictionary<String, Object> dictionary, ZipOutputStream combinedZos) throws IOException {

        String name = catalogMappingSupplier.getClass().getPackageName();
        name = name.substring(46);

        Path zipDir = Files.createDirectories(tempDir.resolve("cubeserver/tutorial/zip"));
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipDir.resolve(name + ".zip").toFile()));

        Bundle b = FrameworkUtil.getBundle(catalogMappingSupplier.getClass());

        OlapCheckModel cm = catalogMappingSupplier.get();



        String grp = (String) dictionary.get("group");
        grp = grp == null ? "Unstrutured" : grp;

        String catName = cm.getName();

        catName = catName.replaceFirst(grp + " - ", "").replaceFirst("Daanse Tutorial - ", "");


        String kind = (String) dictionary.get("kind");
        kind = kind == null ? "other" : kind;

        String nr = (String) dictionary.get("number");
        nr = nr == null ? "z" + i : nr;

        OlapCheckModel c = cm;

        URI uriCatalog = URI.createFileURI("catalog.xmi");
        Resource resourceCatalog = resourceSet.createResource(uriCatalog);

        Set<EObject> set = new HashSet<>();

        set = allRef(set, c);

        // sort

        List<EObject> sortedList = set.stream().sorted(comparator).toList();


        for (EObject eObject : sortedList) {

            if (eObject.eContainer() == null) {
                resourceCatalog.getContents().add(eObject);
            }

        }
        Map<Object, Object> options = new HashMap<>();
        options.put(XMLResource.OPTION_ENCODING, "UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resourceCatalog.save(baos, options);

        ZipEntry entry = new ZipEntry(name + "/mapping/catalog.xmi");
        zos.putNextEntry(entry);
        zos.write(baos.toByteArray());
        zos.closeEntry();

        // Add to combined ZIP
        ZipEntry combinedEntry = new ZipEntry(name + "/mapping/catalog.xmi");
        combinedZos.putNextEntry(combinedEntry);
        combinedZos.write(baos.toByteArray());
        combinedZos.closeEntry();

        Files.createDirectories(zipDir);

        zos.close();
    }

    private Set<EObject> allRef(Set<EObject> set, EObject eObject) {

        if (set.add(eObject)) {

            TreeIterator<EObject> allContents = eObject.eAllContents();
            while (allContents.hasNext()) {
                EObject obj = allContents.next();

                set = allRef(set, obj);
            }

            for (EObject eObject2 : eObject.eCrossReferences()) {

                set = allRef(set, eObject2);

            }
            EObject eContainer = eObject.eContainer();

            if (eContainer != null) {
                set = allRef(set, eContainer);

            }

        }
        return set;
    }

    static EObjectComparator comparator = new EObjectComparator();

    static class EObjectComparator implements Comparator<EObject> {

        AtomicInteger COUNTER = new AtomicInteger(1);
        Map<EClass, Integer> map = new HashMap<EClass, Integer>();

        EObjectComparator() {
            add(OlapCheckPackage.Literals.CATALOG_CHECK);
            add(OlapCheckPackage.Literals.DATABASE_SCHEMA_CHECK);
            add(OlapCheckPackage.Literals.CUBE_CHECK);
        }

        void add(EClass eClass) {
            map.put(eClass, COUNTER.incrementAndGet());
        }

        @Override
        public int compare(EObject o1, EObject o2) {

            EClass eClass1 = o1.eClass();
            EClass eClass2 = o2.eClass();
            int value = map.getOrDefault(eClass1, 0) - map.getOrDefault(eClass2, 0);

            if (value != 0) {
                return value;
            }

            Object s1 = "";
            Object s2 = "";
            EStructuralFeature eStructuralFeature1 = eClass1.getEStructuralFeature("id");
            if (eStructuralFeature1 != null) {

                s1 = o1.eGet(eStructuralFeature1);
            }
            EStructuralFeature eStructuralFeature2 = eClass2.getEStructuralFeature("id");
            if (eStructuralFeature2 != null) {

                s2 = o2.eGet(eStructuralFeature2);
            }
            if (s1 == null) {
                s1 = "";
            }
            if (s2 == null) {
                s2 = "";
            }

            return s1.toString().compareToIgnoreCase(s2.toString());
        }
    };

    public static void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
