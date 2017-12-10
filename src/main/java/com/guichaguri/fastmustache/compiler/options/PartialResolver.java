package com.guichaguri.fastmustache.compiler.options;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Guichaguri
 */
@FunctionalInterface
public interface PartialResolver {

    /**
     * Resolves a partial
     *
     * @param template The template name
     * @param partialName The partial name
     * @return The partial template reader or {@code null} if the partial can't be resolved
     */
    Reader resolve(String template, String partialName) throws IOException;

}
