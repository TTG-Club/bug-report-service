package club.ttg.bug.report.urlhistory;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

class PageUrlRewriterTest {

    private static BiFunction<String, String, Optional<String>> history(Map<String, String> pairs) {
        return (section, slug) -> Optional.ofNullable(pairs.get(section + "/" + slug));
    }

    private static final BiFunction<String, String, Optional<String>> FIREBALL =
            history(Map.of("spells/fireball-phb", "fireball-ph"));

    @Test
    void replacesOldSlugInPath() {
        assertThat(PageUrlRewriter.rewrite("/spells/fireball-phb", FIREBALL)).isEqualTo("/spells/fireball-ph");
    }

    @Test
    void keepsQueryFragmentAndTrailingSlash() {
        assertThat(PageUrlRewriter.rewrite("/spells/fireball-phb?tab=1#desc", FIREBALL))
                .isEqualTo("/spells/fireball-ph?tab=1#desc");
        assertThat(PageUrlRewriter.rewrite("/spells/fireball-phb/", FIREBALL)).isEqualTo("/spells/fireball-ph/");
    }

    @Test
    void replacesInFullLink() {
        assertThat(PageUrlRewriter.rewrite("https://ttg.club/spells/fireball-phb", FIREBALL))
                .isEqualTo("https://ttg.club/spells/fireball-ph");
    }

    @Test
    void leavesOtherPagesAlone() {
        assertThat(PageUrlRewriter.rewrite("/spells/fireball-xge", FIREBALL)).isEqualTo("/spells/fireball-xge");
        assertThat(PageUrlRewriter.rewrite("/bestiary/fireball-phb", FIREBALL)).isEqualTo("/bestiary/fireball-phb");
        assertThat(PageUrlRewriter.rewrite("/spells", FIREBALL)).isEqualTo("/spells");
        assertThat(PageUrlRewriter.rewrite("/spells?search=fireball-phb", FIREBALL))
                .isEqualTo("/spells?search=fireball-phb");
        assertThat(PageUrlRewriter.rewrite("/characters/fireball-phb/sheet", FIREBALL))
                .isEqualTo("/characters/fireball-phb/sheet");
        assertThat(PageUrlRewriter.rewrite("/spells/fireball-phbx", FIREBALL)).isEqualTo("/spells/fireball-phbx");
        assertThat(PageUrlRewriter.rewrite(null, FIREBALL)).isNull();
        assertThat(PageUrlRewriter.rewrite("", FIREBALL)).isEmpty();
    }

    @Test
    void followsChain() {
        var chain = history(Map.of("spells/a", "b", "spells/b", "c"));
        assertThat(PageUrlRewriter.rewrite("/spells/a", chain)).isEqualTo("/spells/c");
        assertThat(PageUrlRewriter.rewrite("/spells/b", chain)).isEqualTo("/spells/c");
    }

    @Test
    void stopsOnCycle() {
        var cycle = history(Map.of("spells/a", "b", "spells/b", "a"));
        assertThat(PageUrlRewriter.rewrite("/spells/a", cycle)).isEqualTo("/spells/b");
    }

    @Test
    void isIdempotent() {
        String once = PageUrlRewriter.rewrite("/spells/fireball-phb", FIREBALL);
        assertThat(PageUrlRewriter.rewrite(once, FIREBALL)).isEqualTo(once);
    }
}
