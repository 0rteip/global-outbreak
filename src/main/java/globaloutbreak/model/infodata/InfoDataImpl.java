package globaloutbreak.model.infodata;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import globaloutbreak.model.cure.CureData;
import globaloutbreak.model.region.Region;

/**
 * Class to manage Dna Points.
 */
public final class InfoDataImpl implements InfoData {

    private static final int BASE_DEATHS_RANGE = 10_500_000;
    private static final int BASE_INFECTED_RANGE = 10_500_000;
    private static final int INITIAL_POINTS = 1;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private long deathsLimit;
    private long infectedLimit;
    private int dnaPoints;
    private long totalDeaths;
    private long totalInfected;
    private final long totalPopulation;
    private CureData cureData;
    private final Random random = new Random();

    /**
     * Constructor.
     * 
     * @param totalPopulation
     *                        population
     */
    public InfoDataImpl(final long totalPopulation) {
        this.dnaPoints = INITIAL_POINTS;
        this.infectedLimit = BASE_INFECTED_RANGE;
        this.deathsLimit = BASE_DEATHS_RANGE;
        this.totalPopulation = totalPopulation;
    }

    /**
     * Increase Dna Points.
     * 
     * @param points
     *               points to be added.
     */
    @Override
    public void increasePoints(final int points) {
        if (checkIfPositive(points)) {
            this.dnaPoints += points;
        } else {
            throw new IllegalArgumentException("The value to be added must be positive.");
        }
    }

    /**
     * Decrease Dna Points.
     * 
     * @param points
     *               points to be decreased.
     */
    @Override
    public void decreasePoints(final int points) {
        if (checkIfPositive(points)) {
            this.dnaPoints -= points;
        } else {
            throw new IllegalArgumentException("The value to be decreased must be positive.");
        }
    }

    /**
     * @return
     *         the points owned.
     */
    @Override
    public int getPoints() {
        return this.dnaPoints;
    }

    /**
     * check if a value is greater than 1.
     * 
     * @param value
     *              the value to be checked.
     * @return
     *         true if the value is greater than 1, false otherwise.
     */
    private boolean checkIfPositive(final int value) {
        if (value < 1) {
            logger.error("The value to be added or decreased must be positive.");
            return false;
        }
        return true;
    }

    @Override
    public long getTotalDeaths() {
        return this.totalDeaths;
    }

    @Override
    public long getTotalInfected() {
        return this.totalInfected;
    }

    @Override
    public long getTotalPopulation() {
        return this.totalPopulation;
    }

    @Override
    public CureData getCureData() {
        return this.cureData;
    }

    @Override
    public void updateTotalDeathsAndInfected(final List<Region> regions) {
        this.totalDeaths = regions.stream()
                .map(Region::getNumDeath)
                .reduce(0L, (e0, e1) -> e0 + e1);
        if (this.totalDeaths > this.deathsLimit) {
            this.increasePoints(random.nextInt(3) + 1);
            this.deathsLimit += BASE_DEATHS_RANGE;
        }
        this.totalInfected = regions.stream()
                .map(Region::getNumInfected)
                .reduce(0L, (e0, e1) -> e0 + e1);
        if (this.totalInfected > this.infectedLimit) {
            this.increasePoints(INITIAL_POINTS);
            this.infectedLimit += BASE_INFECTED_RANGE;
        }
    }

    @Override
    public void updateCureData(final CureData cureData) {
        this.cureData = cureData;
    }

    @Override
    public void updateDeaths(final long deaths) {
        this.totalDeaths += deaths;
    }
}
