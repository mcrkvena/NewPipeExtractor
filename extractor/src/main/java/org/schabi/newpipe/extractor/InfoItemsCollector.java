package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class InfoItemsCollector<I extends InfoItem, E extends InfoItemExtractor>
        implements Collector<I, E> {

    private final List<I> itemList = new ArrayList<>();
    private final List<Throwable> errors = new ArrayList<>();
    private final int serviceId;
    @Nullable
    private final Comparator<I> comparator;

    /**
     * Create a new collector with no comparator / sorting function
     * @param serviceId the service id
     */
    public InfoItemsCollector(final int serviceId) {
        this(serviceId, null);
    }

    /**
     * Create a new collector
     * @param serviceId the service id
     */
    public InfoItemsCollector(final int serviceId, @Nullable final Comparator<I> comparator) {
        this.serviceId = serviceId;
        this.comparator = comparator;
    }

    @Override
    public List<I> getItems() {
        if (comparator != null) {
            itemList.sort(comparator);
        }
        return Collections.unmodifiableList(itemList);
    }

    @Override
    public List<Throwable> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public void reset() {
        itemList.clear();
        errors.clear();
    }

    /**
     * Add an error
     * @param error the error
     */
    protected void addError(final Exception error) {
        errors.add(error);
    }

    /**
     * Add an item
     * @param item the item
     */
    protected void addItem(final I item) {
        itemList.add(item);
    }

    /**
     * Get the service id
     * @return the service id
     */
    public int getServiceId() {
        return serviceId;
    }

    @Override
    public void commit(final E extractor) {
        try {
            addItem(extract(extractor));
        } catch (final FoundAdException ae) {
            // found an ad. Maybe a debug line could be placed here
        } catch (final ParsingException e) {
            addError(e);
        }
    }
}
