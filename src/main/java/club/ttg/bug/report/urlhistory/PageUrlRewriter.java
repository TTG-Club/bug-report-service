package club.ttg.bug.report.urlhistory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Замена прежнего адреса записи каталога в адресе страницы баг-репорта на текущий.
 * <p>
 * Сайт присылает адрес страницы как путь ({@code /spells/fireball-phb}, иногда с
 * {@code ?…} или {@code #…}); старые репорты могут хранить полную ссылку
 * ({@code https://ttg.club/spells/fireball-phb}). Страница записи каталога — ровно
 * два сегмента пути: раздел и адрес записи. Всё остальное (хост, хвост после
 * {@code ?}/{@code #}, завершающий {@code /}) сохраняется как было.
 */
public final class PageUrlRewriter {

    /** Предохранитель от зацикленной истории адресов. */
    private static final int MAX_HOPS = 10;

    private static final Pattern ENTITY_PAGE =
            Pattern.compile("^((?:[a-zA-Z][a-zA-Z0-9+.-]*://[^/?#]+)?)/([^/?#]+)/([^/?#]+)(/?)([?#].*)?$");

    private PageUrlRewriter() {
    }

    /**
     * @param url    адрес страницы из баг-репорта (может быть {@code null})
     * @param lookup (раздел, адрес записи) → текущий адрес записи, если адрес прежний
     * @return адрес с текущим адресом записи, либо исходная строка, если менять нечего
     */
    public static String rewrite(String url, BiFunction<String, String, Optional<String>> lookup) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        Matcher matcher = ENTITY_PAGE.matcher(url);
        if (!matcher.matches()) {
            return url;
        }
        String section = matcher.group(2);
        String slug = matcher.group(3);

        // История в core-api схлопнута, так что обычно хватает одного шага; цикл —
        // на случай, если локальная копия ещё не догнала цепочку A→B→C.
        String current = slug;
        Set<String> seen = new HashSet<>();
        seen.add(current);
        for (int hop = 0; hop < MAX_HOPS; hop++) {
            Optional<String> next = lookup.apply(section, current);
            if (next.isEmpty() || !seen.add(next.get())) {
                break;
            }
            current = next.get();
        }
        if (current.equals(slug)) {
            return url;
        }
        return matcher.group(1) + "/" + section + "/" + current + matcher.group(4)
                + (matcher.group(5) == null ? "" : matcher.group(5));
    }
}
