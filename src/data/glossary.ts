/**
 * Glossary of terms and abbreviations
 *
 * Add terms here to automatically highlight them across the site
 * with a dotted underline and hover tooltip.
 *
 * Definitions should not have a period at the end
 *
 *   @property definition: The explanation shown on hover
 *   @property caseSensitive: If true, match this term only in its exact casing (e.g. "CAN" won't match "can"). Defaults to false.
 *   @property term: The word/abbreviation to match (case-insensitive unless caseSensitive is true)
 */

export interface GlossaryTerm {
    /**
     * The word/abbreviation to match (case-insensitive)
     * @example "SystemCore"
     */
    term: string;
    /**
     * The explanation shown on hover
     * @example "Main processor for robot code, contains various IO"
     */
    definition: string;
    /**
     * If true, match this term only in its exact casing (e.g. "CAN" won't match "can").
     * @default false
     */
    caseSensitive?: boolean;
}

export const glossaryTerms: GlossaryTerm[] = [
    // Electronics
    {
        term: 'PDH',
        definition: 'Power Distribution Hub',
    },
    {
        term: 'Spark MAX',
        definition: 'Motor controller for REV motors',
    },
    {
        term: 'Talon FX',
        definition: 'Motor controller for CTRE motors',
    },
    {
        term: 'SystemCore',
        definition: 'Main processor for robot code, contains various IO',
    },
    {
        term: 'CAN',
        definition:
            'Controller Area Network: typically yellow and green cable used to communicate with motor controllers and sensors, can be run in various topographies instead of each cable needing to connect to SystemCore',
        caseSensitive: true,
    },
    {
        term: 'PWM',
        definition:
            'Pulse Width Modulation: A communication spec used to communicate with motor controllers and sensors, needs to connect back to SystemCore',
    },
    {
        term: 'Main Breaker',
        definition: 'Power switch for the robot',
    },
    {
        term: 'Limit Switch',
        definition:
            'Type of sensor that triggers when physically or magnetically hit. Can be used to trigger actions on a rising or falling edge, or check state of a mechanism',
    },
    {
        term: 'Throughbore Encoder',
        definition:
            'An encoder that allows shafts to pass through its center to record angular position',
    },
    {
        term: 'Magnetic Encoder',
        definition:
            'An encoder that uses a receiver and a magnet to measure position and motion',
    },

    // Software
    {
        term: 'Repository',
        definition:
            'A storage location for your code and related assets, often used in version control systems like Git. Repositories are just folders that contain files and subfolders, and they can be hosted on platforms like GitHub to facilitate collaboration and version tracking',
    },
    {
        term: 'CI',
        definition:
            'Continuous Integration: A software development practice where developers frequently merge code changes into a shared repository, triggering automated builds and tests to ensures the codebase remains stable',
    },
    {
        term: 'WPILib',
        definition:
            'WPILib is the standard software library and toolsuite provided for teams to write, test, and debug code for their FIRST® Robotics Competition and FIRST® Tech Challenge robots',
    },
    {
        term: 'GitHub',
        definition:
            'GitHub is like Google Drive, but for code. It hosts git repositories and allows for improved collaboration on projects through pull requests and issues',
    },
    {
        term: 'Heading',
        definition:
            'The direction the robot is pointed, usually expressed as an angle in degrees',
    },
    {
        term: 'Odometry',
        definition:
            'Using sensors on the robot to create an estimate of the pose of the robot on the field.',
    },
];

/**
 * Get a glossary term by its name (case-insensitive)
 */
export function getGlossaryTerm(term: string): GlossaryTerm | undefined {
    return glossaryTerms.find(
        (g) => g.term.toLowerCase() === term.toLowerCase(),
    );
}

/**
 * Get all terms as a map for quick lookup
 */
export function getGlossaryMap(): Map<string, string> {
    const map = new Map<string, string>();
    glossaryTerms.forEach(({ term, definition }) => {
        map.set(term.toLowerCase(), definition);
    });
    return map;
}
