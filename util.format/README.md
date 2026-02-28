# org.eclipse.daanse.olap.util.format

VB-style formatting engine for numbers, strings, and dates. Implements formatting compatible with Visual Basic's `Format()` function, supporting locale-aware output. This is the primary formatting engine used by the OLAP `Format()` MDX function.

## Usage

```java
import org.eclipse.daanse.olap.util.format.Format;

// Direct instantiation
Format format = new Format("##,##0.00", Locale.US);
String result = format.format(1234.5);  // "1,234.50"

// Cached factory (preferred for repeated use - avoids re-parsing)
Format format = Format.get("##,##0.00", Locale.US);
```

## Public API

| Class | Role |
|-------|------|
| `Format` | Entry point. Parses a VB-style format string and formats values (`double`, `long`, `String`, `Date`, `Calendar`, `BigDecimal`, `LocalDateTime`). Provides an LRU-cached factory via `Format.get(formatString, locale)`. |
| `FormatLocale` | Immutable locale configuration: thousand separator, decimal placeholder, date/time separators, currency symbol/format, day/month names, AM/PM strings. Created via `Format.createLocale(Locale)`. |
| `FormatToken` | Represents a parsed token from a format string (digit placeholder, date component, string modifier, or literal). |

## Format String Categories

### Numeric

Placeholders: `0` (digit or zero), `#` (digit or nothing), `.` (decimal point), `,` (thousand separator), `%` (percent), `E+`/`e-` (scientific notation).

Up to four semicolon-separated sections control output by value sign:

```
positive ; negative ; zero ; null
```

Example:

```java
new Format("##,##0.###;(##,##0.###);;Nil", locale)
// 1234.5   -> "1,234.5"
// -1234.5  -> "(1,234.5)"
// 0        -> ""
// null     -> "Nil"
```

Thousand-separator scaling: trailing commas divide by 1000 per comma (e.g., `"##0,,"` shows millions).

Percent: `%` multiplies by 100 (e.g., `"0.00%"` formats 0.5 as `"50.00%"`).

### Date/Time

| Token | Meaning |
|-------|---------|
| `d` | Day without leading zero (1-31) |
| `dd` | Day with leading zero (01-31) |
| `ddd` / `Ddd` | Abbreviated day name (Sun-Sat) |
| `dddd` | Full day name (Sunday-Saturday) |
| `ddddd` | Short date (system format, default m/d/yy) |
| `dddddd` | Long date (mmmm dd, yyyy) |
| `w` | Day of week as number (1=Sunday) |
| `ww` | Week of year (1-53) |
| `m` | Month without leading zero (1-12); minute if after `h`/`hh` |
| `mm` | Month with leading zero (01-12); minute if after `h`/`hh` |
| `mmm` | Abbreviated month (Jan-Dec) |
| `mmmm` | Full month (January-December) |
| `q` | Quarter (1-4) |
| `y` | Day of year (1-366) |
| `yy` | 2-digit year (00-99) |
| `yyyy` | 4-digit year |
| `h` | Hour without leading zero (0-23, or 0-12 with AM/PM) |
| `hh` | Hour with leading zero |
| `HH` | Hour with leading zero (always 24-hour) |
| `n` | Minute without leading zero |
| `nn` | Minute with leading zero |
| `s` | Second without leading zero |
| `ss` | Second with leading zero |
| `ttttt` | Full time (h:mm:ss) |
| `AM/PM` | 12-hour clock with uppercase AM/PM |
| `am/pm` | 12-hour clock with lowercase am/pm |
| `A/P` | 12-hour clock with A/P |
| `AMPM` | AM/PM using system locale strings |
| `:` | Time separator (locale-dependent) |
| `/` | Date separator (locale-dependent) |

Example:

```java
new Format("dd/mm/yyyy hh:nn:ss AM/PM", locale)
```

### String

| Token | Meaning |
|-------|---------|
| `@` | Character or space |
| `&` | Character or nothing |
| `>` | Force uppercase |
| `<` | Force lowercase |
| `!` | Fill from left (default is right-to-left) |

### Literals and Escaping

- `\x` displays the next character `x` literally
- `"text"` displays text inside double quotes literally
- Characters `-`, `+`, `$`, `(`, `)`, ` ` are displayed literally

### Named Macros

Predefined format names that expand to standard patterns:

| Name | Expands To |
|------|-----------|
| `Currency` | Locale currency format with parentheses for negatives |
| `Fixed` | `0` |
| `Standard` | `#,##0` |
| `Percent` | `0.00%` |
| `Scientific` | `0.00e+00` |
| `Long Date` | `dddd, mmmm dd, yyyy` |
| `Medium Date` | `dd-mmm-yy` |
| `Short Date` | `m/d/yy` |
| `Long Time` | `h:mm:ss AM/PM` |
| `Medium Time` | `h:mm AM/PM` |
| `Short Time` | `hh:mm` |
| `Yes/No` | Non-zero = "Yes", zero = "No" |
| `True/False` | Non-zero = "True", zero = "False" |
| `On/Off` | Non-zero = "On", zero = "Off" |

## Locale Handling

`FormatLocale` instances are created automatically from a `java.util.Locale` via `Format.createLocale(Locale)` and cached internally. The locale determines:

- Thousand separator (e.g., `,` in US, `.` in Germany)
- Decimal placeholder (e.g., `.` in US, `,` in France)
- Date separator (e.g., `/` in US)
- Time separator (e.g., `:` in US)
- Currency symbol and format pattern
- Day and month names (short and long forms)
- AM/PM strings

Custom locales can be registered via `Format.createLocale(char, char, String, String, String, String, List, List, List, List, Locale)`.

If the Java locale cannot resolve a currency symbol (returns the international currency symbol `\u00a4`), the system default locale's currency symbol is used as fallback.

## Thread Safety

- `Format` instances are effectively immutable after construction and safe for concurrent use.
- `Format.get(String, Locale)` uses a thread-safe bounded LRU cache (max 1000 entries).
- The `FormatLocale` registry is backed by a `ConcurrentHashMap`.

## Package Structure

```
org.eclipse.daanse.olap.util.format           -- public API (exported)
    Format                                     -- entry point, parser, cache
    FormatLocale                               -- locale configuration
    FormatToken                                -- parsed format token

org.eclipse.daanse.olap.util.format.internal   -- implementation (NOT exported)
    BasicFormat                                -- base class for format strategies
    AlternateFormat                            -- positive/negative/zero/null selection
    CompoundFormat                             -- sequential chain of formats
    NumericFormat                              -- VB-style number formatting
    JavaFormat                                 -- delegates to java.text formatters
    VbDateFormat                               -- VB-style date/time formatting
    LiteralFormat                              -- constant string output
    FallbackFormat                             -- prints token for unhandled types
    StringFormat                               -- upper/lower case conversion
    FormatConstants                            -- token codes, flags, token table
    MacroToken                                 -- named format macro expansion
    FormatType                                 -- enum: STRING, DATE, NUMERIC
    DaanseFloatingDecimal                      -- custom floating decimal representation
    DigitList                                  -- radix-10 digit array (forked from ICU)
    LruCache                                   -- bounded LRU cache
    StringCase                                 -- enum: UPPER, LOWER
```

## Internal Design

### Class Hierarchy

```
BasicFormat                     -- base, throws for unsupported types
  +-- JavaFormat                -- java.text.NumberFormat / DateFormat delegation
  |     +-- NumericFormat       -- VB numeric formatting (#, 0, comma, E+)
  +-- LiteralFormat             -- constant string output
  +-- FallbackFormat (abstract) -- prints token string for unhandled types
  |     +-- VbDateFormat        -- VB date/time formatting
  +-- StringFormat              -- case conversion (> / <)
  +-- CompoundFormat            -- chains formats sequentially (composite)
  +-- AlternateFormat           -- selects format by value sign (strategy)
```

### Design Patterns

- **Composite**: `CompoundFormat` chains format elements (e.g., month + separator + day + separator + year) into a single format that applies each element in sequence.
- **Strategy**: `AlternateFormat` selects among up to four sub-formats based on the sign and nullity of the value, following VB's semicolon-separated section rules.

### Numeric Internals

`NumericFormat` uses `DaanseFloatingDecimal` (backed by `DigitList` from ICU) for precise digit manipulation. It supports:

- Configurable leading/trailing zeroes
- Thousand separator with custom grouping positions
- Exponential notation (`E+`, `E-`, `e+`, `e-`)
- Decimal shifting for `%` (multiply by 100) and trailing `,` (divide by 1000)
- Half-even (banker's) rounding
