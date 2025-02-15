package globaloutbreak.model.cure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import globaloutbreak.model.region.Region;
import globaloutbreak.model.cure.prioriry.Priority;

/**
 * SimpleCure is a basic implementation of {@link Cure}.
 */
public final class SimpleCure implements Cure {

    private final Logger logger = LoggerFactory.getLogger(SimpleCure.class);

    private final Map<Region, Float> contributions;
    private final Set<Integer> rilevantProgress;
    private final List<Priority> priorities;
    private final float researchersEfficiency;
    private final float dailyBudget;
    private final int numberOfMajorContributors;
    private float necessaryBudget;
    private float researchBudget;
    private int daysBeforeStartResearch;
    private int currentPriority;
    private boolean isStarted;
    private boolean isComplete;
    private Optional<Consumer<Integer>> action;

    private SimpleCure(final float dailyBudget, final int numberOfMajorContributors,
            final Map<Region, Float> contributions, final float researchersEfficiency, final List<Priority> priorities,
            final float necessaryBudget, final float researchBudget, final int currentPriority,
            final int daysBeforeStartResearch, final Set<Integer> rilevantProgress) {
        this.dailyBudget = dailyBudget;
        this.numberOfMajorContributors = numberOfMajorContributors;
        this.contributions = contributions;
        this.researchersEfficiency = researchersEfficiency;
        this.priorities = priorities;
        this.necessaryBudget = necessaryBudget;
        this.researchBudget = researchBudget;
        this.currentPriority = currentPriority;
        this.daysBeforeStartResearch = daysBeforeStartResearch;
        this.rilevantProgress = rilevantProgress;
        this.isStarted = false;
        this.isComplete = false;
    }

    @Override
    public void addAction(final Consumer<Integer> action) {
        this.action = Optional.of(action);
    }

    @Override
    public CureData getGlobalStatus() {
        return new CureData() {

            @Override
            public int getProgress() {
                return cureProgress();
            }

            @Override
            public Optional<Integer> getRemainingDays() {
                final float dailyInvestment = contributions.entrySet().stream()
                        .filter(el -> el.getKey().getCureStatus() == RegionCureStatus.STARTED)
                        .map(el -> dailyRegionContribution(el.getKey()))
                        .reduce(0f, (f0, f1) -> f0 + f1);
                return dailyInvestment != 0
                        ? Optional.of(Math.round((necessaryBudget - researchBudget) / dailyInvestment))
                        : Optional.empty();
            }

            @Override
            public List<Region> getMajorContributors() {
                return isStarted ? contributions.entrySet().stream()
                        .sorted((e0, e1) -> Float.compare(e1.getValue(), e0.getValue()))
                        .map(el -> el.getKey())
                        .limit(numberOfMajorContributors)
                        .toList() : new ArrayList<>();
            }

            @Override
            public Priority gePriority() {
                return priorities.get(currentPriority);
            }

            @Override
            public String toString() {
                return "CureData [" + gePriority() + ", progress=" + getProgress() + ", contrib="
                        + getMajorContributors() + ", days=" + getRemainingDays() + "]";
            }
        };
    }

    @Override
    public void research() {
        if (this.isStarted) {
            this.updateResearch();
        } else {
            this.checkForDisease();
        }

        this.notifyIfNecessary();

        if (this.cureProgress() >= 100) {
            this.isComplete = true;
        }
    }

    private void updateResearch() {
        // if the research has started every region contributes to the research
        this.contributions.entrySet().stream()
                .filter(el -> el.getKey().getCureStatus() == RegionCureStatus.STARTED)
                .forEach(el -> this.contributions.compute(
                        el.getKey(),
                        (key, val) -> val + this.dailyRegionContribution(key)));
        this.updateResearchBudget();
        if (this.highMortalityRateRegions().count() > 0) {
            this.increasePriority();
        }
    }

    private void checkForDisease() {
        // if a region has discovered the disease and after the
        // 'daysBeforeStartResearch', each Region starts looking for a cure
        if (this.numberOfRegionsTahtDiscoveredDisease() > 0 && this.daysBeforeStartResearch == 0) {
            this.startCureResearch();
        } else {
            this.checkRegionStatus();
        }
    }

    private void startCureResearch() {
        this.logger.info("Start Cure research");
        this.isStarted = true;
        this.increasePriority();
        this.contributions.entrySet().stream()
                .filter(el -> el.getKey().getDeathByVirus() != el.getKey().getPopTot())
                .forEach(el -> el.getKey().setCureStatus(RegionCureStatus.STARTED));
    }

    private void checkRegionStatus() {
        if (this.highMortalityRateRegions().count() > 0) {
            this.daysBeforeStartResearch--;
            this.highMortalityRateRegions()
                    .filter(el -> el.getKey().getDeathByVirus() != el.getKey().getPopTot())
                    .forEach(el -> el.getKey().setCureStatus(RegionCureStatus.DISCOVERED));
        }
    }

    private void notifyIfNecessary() {
        final Optional<Integer> min = this.rilevantProgress.stream()
                .min(Integer::compareTo)
                .filter(el -> el <= this.cureProgress());
        if (min.isPresent()) {
            action.ifPresent(a -> a.accept(min.get()));
            this.rilevantProgress.remove(min.get());
        }
    }

    @Override
    public boolean isCompleted() {
        return this.isComplete;
    }

    @Override
    public void increaseResearchDifficulty(final float changeFactor) {
        this.necessaryBudget *= 1 + changeFactor;
    }

    @Override
    public void reduceResearchProgress(final float changeFactor) {
        this.researchBudget *= 1 + changeFactor;
    }

    private float dailyRegionContribution(final Region region) {
        return (1 - Float.valueOf(region.getDeathByVirus()) / region.getPopTot())
                * region.getFacilities()
                * this.researchersEfficiency
                * this.priorities.get(this.currentPriority).getResourcesPercentage()
                * this.dailyBudget;
    }

    private int cureProgress() {
        final int progress = Math.round(this.researchBudget / this.necessaryBudget * 100);
        if (progress >= 100) {
            this.researchBudget = this.necessaryBudget;
            return 100;
        }
        return progress;
    }

    private int numberOfRegionsTahtDiscoveredDisease() {
        return Math.toIntExact(this.contributions.entrySet().stream()
                .filter(el -> el.getKey().getCureStatus() == RegionCureStatus.DISCOVERED)
                .count());
    }

    private void increasePriority() {
        this.priorities.stream()
                .filter(el -> el.getPriority() == this.currentPriority + 1)
                .findAny().ifPresent(el -> {
                    this.currentPriority = el.getPriority();
                    this.logger.info("Research priority: " + this.priorities.get(currentPriority));
                });
    }

    private void updateResearchBudget() {
        this.researchBudget = this.contributions.entrySet().stream()
                .map(el -> el.getValue())
                .reduce(Float.valueOf(0), (f0, f1) -> f0 + f1);
    }

    private Stream<Entry<Region, Float>> highMortalityRateRegions() {
        return this.contributions.entrySet().stream()
                .filter(el -> Float.valueOf(el.getKey().getDeathByVirus())
                        / el.getKey().getPopTot() > this.priorities.get(this.currentPriority)
                                .getDetectionRate());
    }

    @Override
    public boolean isConsistent() {
        if (this.priorities.isEmpty()) {
            logger.warn("Priority list can't be empty");
            return false;
        }
        if (this.contributions.isEmpty()) {
            logger.warn("Regions list can't be empty");
            return false;
        }
        if (this.priorities.stream()
                .filter(el -> el.getPriority() == this.currentPriority).count() != 1) {
            logger.warn("Invalid current prioriry: current priority '{}' is not found in the priorities '{}'",
                    this.currentPriority, this.priorities);
            return false;
        }
        if (this.necessaryBudget < this.researchBudget) {
            logger.warn("Research budget '{}' must be lower than necessary budget '{}'",
                    this.researchBudget, this.necessaryBudget);
            return false;
        }

        return this.checkIfPositive(this.dailyBudget, "dailyBudget")
                && this.checkIfPositive(this.numberOfMajorContributors, "numberOfMajorContributors")
                && this.checkIfPositive(this.researchersEfficiency, "researchersEfficiency")
                && this.checkIfPositive(this.necessaryBudget, "necessaryBudget")
                && this.checkIfPositive(this.researchBudget, "researchBudget")
                && this.checkIfPositive(this.currentPriority, "currentPriority")
                && this.checkIfPositive(this.daysBeforeStartResearch, "daysBeforeStartResearch");
    }

    private boolean checkIfPositive(final float number, final String name) {
        if (number < 0) {
            logger.warn("Value {} can't be negative", name);
            return false;
        }
        return true;
    }

    /**
     * Pattern builder: used here because:
     * 
     * - all the parameters of the SimpleCure class have a default value, execpt for
     * priorities and contributions, and we would like to have all the possible
     * contructors for the cure, to specialize it, too many to separate them all.
     * 
     * - multiple parameters are of type float and it means that not all the
     * costructor
     * could exist.
     * 
     * - some value could be either be confused when inizialized.
     * 
     */
    @SuppressWarnings("PMD.LinguisticNaming")
    public static class Builder {

        private static final float DAILY_BUDGET = 1_146.56f;
        private static final int NUMBER_OF_MAJOR_CONTRIBUTORS = 3;
        private static final float RESEARCHERS_EFFICIENCY = 1;
        private static final float NECESSARY_BUDGET = 25_000_000;
        private static final float RESEARCH_BUDGET = 0;
        private static final int CURRENT_PRIORITY = 0;
        private static final int DAYS_BEFORE_START_RESEARCH = 10;
        private static final Set<Integer> RILEVANT_PROGRESS = new HashSet<>();

        private float dailyBudget = DAILY_BUDGET;
        private int numberOfMajorContributors = NUMBER_OF_MAJOR_CONTRIBUTORS;
        private float researchersEfficiency = RESEARCHERS_EFFICIENCY;
        private float necessaryBudget = NECESSARY_BUDGET;
        private float researchBudget = RESEARCH_BUDGET;
        private int currentPriority = CURRENT_PRIORITY;
        private int daysBeforeStartResearch = DAYS_BEFORE_START_RESEARCH;
        private Set<Integer> rilevantProgreass = RILEVANT_PROGRESS;
        private final List<Priority> priorities;
        private final Map<Region, Float> contributions = new HashMap<>();
        private boolean consumed;

        /**
         * The Builder for a {@link SimpleCure} which is managed in the given
         * {@link Region}
         * and which has the given {@link Priority} levels.
         * 
         * @param regions
         *                   Regions that research for the cure
         * @param priorities
         *                   Priority types
         */
        public Builder(final List<Region> regions, final List<Priority> priorities) {
            if (!regions.isEmpty()) {
                regions.forEach(el -> this.contributions.put(el, 0f));
            }
            this.priorities = new ArrayList<>(priorities);
        }

        /**
         * @param dailyBudget the daily budget that every facility can use at max
         * @return this builder, for method chaining
         */
        public Builder setDailyBudget(final float dailyBudget) {
            this.dailyBudget = dailyBudget;
            return this;
        }

        /**
         * @param numberOfMajorContributors the number of the states with the major
         *                                  contribution to the cure
         * @return this builder, for method chaining
         */
        public Builder setNumberOfMajorContributors(final int numberOfMajorContributors) {
            this.numberOfMajorContributors = numberOfMajorContributors;
            return this;
        }

        /**
         * @param researchersEfficiency the efficiency of the researchers
         * @return this builder, for method chaining
         */
        public Builder setResearchersEfficiency(final float researchersEfficiency) {
            this.researchersEfficiency = researchersEfficiency;
            return this;
        }

        /**
         * @param necessaryBudget the necessary budget to complete the cure
         * @return this builder, for method chaining
         */
        public Builder setNecessaryBudget(final float necessaryBudget) {
            this.necessaryBudget = necessaryBudget;
            return this;
        }

        /**
         * @param researchBudget the research budget
         * @return this builder, for method chaining
         */
        public Builder setResearchBudget(final float researchBudget) {
            this.researchBudget = researchBudget;
            return this;
        }

        /**
         * @param currentPriority the current priority
         * @return this builder, for method chaining
         */
        public Builder setCurrentPriority(final int currentPriority) {
            this.currentPriority = currentPriority;
            return this;
        }

        /**
         * @param daysBeforeStartResearch days between discovery and research
         * @return this builder, for method chaining
         */
        public Builder setDaysBeforeStartResearch(final int daysBeforeStartResearch) {
            this.daysBeforeStartResearch = daysBeforeStartResearch;
            return this;
        }

        /**
         * @param rilevantProgreass progress percentage to notify status
         * @return this builder, for method chaining
         */
        public Builder setRilevantProgress(final Set<Integer> rilevantProgreass) {
            this.rilevantProgreass = new HashSet<>(rilevantProgreass);
            return this;
        }

        /**
         * @return a SimpleCure
         */
        public final SimpleCure build() {
            if (consumed) {
                throw new IllegalStateException("The builder can only be used once");
            }
            consumed = true;

            return new SimpleCure(dailyBudget, numberOfMajorContributors, contributions, researchersEfficiency,
                    priorities, necessaryBudget, researchBudget, currentPriority, daysBeforeStartResearch,
                    rilevantProgreass);
        }
    }
}
