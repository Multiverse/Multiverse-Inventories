package org.mvplugins.multiverse.inventories;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TestCommentedYamlConfiguration {
    private static final char LINE_SEPARATOR = '\n';
    private static final File TEST_CONFIG = new File("bin/test/testconfig.yml");

    private static final String TEST_CONTENTS_1 = "# A Test Yaml File" + LINE_SEPARATOR + LINE_SEPARATOR +
            "test: 123" + LINE_SEPARATOR +
            "a_map:" + LINE_SEPARATOR +
            "  something: yep" + LINE_SEPARATOR +
            "  something_else: 42" + LINE_SEPARATOR +
            "  one_more_something_else: 24" + LINE_SEPARATOR +
            "  a_list:" + LINE_SEPARATOR +
            "  - 1" + LINE_SEPARATOR +
            "  - 2" + LINE_SEPARATOR +
            "  a_child_map:" + LINE_SEPARATOR +
            "    another_map:" + LINE_SEPARATOR +
            "      test: 123" + LINE_SEPARATOR +
            "  two_steps_back:" + LINE_SEPARATOR +
            "    test: true" + LINE_SEPARATOR +
            "back_to_root: true" + LINE_SEPARATOR;

    private static final String COMMENTED_TEST_CONTENTS_1 = "# A Test Yaml File" + LINE_SEPARATOR +
            LINE_SEPARATOR +
            "# Yay" + LINE_SEPARATOR +
            "test: 123" + LINE_SEPARATOR +
            LINE_SEPARATOR +
            "# They seem to be" + LINE_SEPARATOR +
            "# Working" + LINE_SEPARATOR +
            "a_map:" + LINE_SEPARATOR +
            "  # Yeah, they're working" + LINE_SEPARATOR +
            "  something: yep" + LINE_SEPARATOR +
            "  something_else: 42" + LINE_SEPARATOR +
            "  one_more_something_else: 24" + LINE_SEPARATOR +
            LINE_SEPARATOR +
            "  # Aww yeah, comments on a list" + LINE_SEPARATOR +
            "  a_list:" + LINE_SEPARATOR +
            "  - 1" + LINE_SEPARATOR +
            "  - 2" + LINE_SEPARATOR +
            "  a_child_map:" + LINE_SEPARATOR +
            "    # Comments on a child child map" + LINE_SEPARATOR +
            "    another_map:" + LINE_SEPARATOR +
            "      test: 123" + LINE_SEPARATOR +
            LINE_SEPARATOR +
            "  # Two steps back comments" + LINE_SEPARATOR +
            "  two_steps_back:" + LINE_SEPARATOR +
            "    test: true" + LINE_SEPARATOR +
            LINE_SEPARATOR +
            "# Back to root comments" + LINE_SEPARATOR +
            "back_to_root: true" + LINE_SEPARATOR;

    private CommentedYamlConfiguration createConfig(boolean doComments) {
        TEST_CONFIG.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(TEST_CONFIG)) {
            out.println(TEST_CONTENTS_1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        CommentedYamlConfiguration testConfig = new CommentedYamlConfiguration(TEST_CONFIG, doComments);

        testConfig.getConfig().options().header("A Test Yaml File\n");

        testConfig.addComment("test", Arrays.asList("# Yay"));
        testConfig.addComment("a_map", Arrays.asList("# They seem to be", "# Working"));
        testConfig.addComment("a_map.something", Arrays.asList("# Yeah, they're working"));
        testConfig.addComment("a_map.a_list", Arrays.asList("# Aww yeah, comments on a list"));
        testConfig.addComment("a_map.a_child_map.another_map", Arrays.asList("# Comments on a child child map"));
        testConfig.addComment("a_map.two_steps_back", Arrays.asList("# Two steps back comments"));
        testConfig.addComment("back_to_root", Arrays.asList("# Back to root comments"));

        return testConfig;
    }

    @Test
    public void testNoComments() throws Exception {
        CommentedYamlConfiguration testConfig = createConfig(false);
        testConfig.save();

        String uncommentedConfigFile = new String(Files.readAllBytes(TEST_CONFIG.getCanonicalFile().toPath()), StandardCharsets.UTF_8);
        assertEquals(TEST_CONTENTS_1, uncommentedConfigFile);
    }

    @Test
    public void testWithComments() throws Exception {
        CommentedYamlConfiguration testConfig = createConfig(true);
        testConfig.save();

        String commentedConfigFile = new String(Files.readAllBytes(TEST_CONFIG.getCanonicalFile().toPath()), StandardCharsets.UTF_8);
        assertEquals(COMMENTED_TEST_CONTENTS_1, commentedConfigFile);
    }
}
