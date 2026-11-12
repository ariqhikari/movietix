#!/usr/bin/env bash
set -euo pipefail

echo "=================================================="
echo "MovieTix Codebase & Architecture Consistency Audit"
echo "=================================================="
echo ""

ERRORS=0
WARNINGS=0

# 1. Audit Domain Layer Isolation
echo "[1/6] Auditing Domain Layer Isolation (Clean Architecture)..."
# Domain must not import Ktor, DataStore, Compose, or platform-specific packages
FORBIDDEN_DOMAIN_IMPORTS=(
    "io.ktor"
    "androidx.datastore"
    "androidx.compose"
    "platform.UIKit"
    "android.content"
    "android.os"
    "coil3"
)

for pkg in "${FORBIDDEN_DOMAIN_IMPORTS[@]}"; do
    MATCHES=$(grep -rn "import $pkg" shared/src/org/ariqhikari/movietix/domain/ 2>/dev/null || true)
    if [ -n "$MATCHES" ]; then
        echo "  [FAIL] Forbidden import '$pkg' found in domain layer:"
        echo "$MATCHES"
        ERRORS=$((ERRORS + 1))
    fi
done
if [ $ERRORS -eq 0 ]; then
    echo "  [PASS] Domain layer is strictly pure Kotlin (no Ktor, DataStore, Compose, or platform imports)."
fi

# 2. Audit UI Styling Tokens (No hardcoded Color, random fontSize/dp, or emojis in screens)
echo ""
echo "[2/6] Auditing UI Styling Tokens & Zero Emojis Rule..."
# Hardcoded Color(0x...) in presentation screens outside theme
HARDCODED_COLORS=$(grep -rn "Color(0x" shared/src/org/ariqhikari/movietix/presentation/ 2>/dev/null || true)
if [ -n "$HARDCODED_COLORS" ]; then
    echo "  [FAIL] Hardcoded Color(0x...) found in presentation screens:"
    echo "$HARDCODED_COLORS"
    ERRORS=$((ERRORS + 1))
else
    echo "  [PASS] No hardcoded Color(0x...) found in presentation layer."
fi

# Audit emojis in shared/src/
# Emojis regex range check: [\x{1F300}-\x{1F9FF}]
EMOJI_MATCHES=$(grep -rn -P "[\x{1F300}-\x{1FAFF}]" shared/src/ 2>/dev/null || true)
if [ -n "$EMOJI_MATCHES" ]; then
    echo "  [FAIL] Emojis detected in source code:"
    echo "$EMOJI_MATCHES"
    ERRORS=$((ERRORS + 1))
else
    echo "  [PASS] Zero emojis in source code."
fi

# 3. Audit Secret & Token Leaks
echo ""
echo "[3/6] Auditing Secret & Token Leaks in Code & Git History..."
# Construct token signature dynamically to prevent self-match
JWT_SIGNATURE=$(printf "eyJ%s" "hbGciOiJIUzI1NiJ")

CODE_SECRETS=$(grep -rn -E "${JWT_SIGNATURE}[a-zA-Z0-9_-]+" shared/ iosApp/ androidApp/ 2>/dev/null | grep -v "Secrets.kt" || true)
if [ -n "$CODE_SECRETS" ]; then
    echo "  [FAIL] Exposed JWT token detected in tracked code files!"
    ERRORS=$((ERRORS + 1))
else
    echo "  [PASS] No exposed JWT tokens found in tracked code files."
fi

# Git history scan for secrets in application code
GIT_SECRET_COMMITS=$(git log -S "$JWT_SIGNATURE" --pretty=format:"%h %an %s" -- shared/ androidApp/ iosApp/ | head -n 10 || true)
if [ -n "$GIT_SECRET_COMMITS" ]; then
    echo "  [FAIL] Potential JWT secret found in git commit history:"
    echo "$GIT_SECRET_COMMITS"
    ERRORS=$((ERRORS + 1))
else
    echo "  [PASS] Clean git history: zero TMDB tokens in commit logs."
fi

# 4. Audit Expect/Actual and Dead Placeholders
echo ""
echo "[4/6] Auditing expect/actual and Dead Code / TODOs..."
EXPECT_DECLS=$(grep -rn "^expect " shared/src/ 2>/dev/null || true)
echo "  Found expect declarations:"
echo "$EXPECT_DECLS"

TODO_FIXME=$(grep -rn -E "(TODO|FIXME)" shared/src/ 2>/dev/null || true)
if [ -n "$TODO_FIXME" ]; then
    echo "  [WARN] Unresolved TODO/FIXME found:"
    echo "$TODO_FIXME"
    WARNINGS=$((WARNINGS + 1))
else
    echo "  [PASS] No TODO or FIXME markers in shared code."
fi

# Check for obsolete ThemePreviewPlaceholder
if [ -f "shared/src/org/ariqhikari/movietix/core/presentation/components/ThemePreviewPlaceholder.kt" ]; then
    echo "  [WARN] Obsolete ThemePreviewPlaceholder.kt is still present."
    WARNINGS=$((WARNINGS + 1))
fi

# 5. Audit MVI / UDF Contracts
echo ""
echo "[5/6] Auditing MVI Contracts (State, Action, Effect) across features..."
FEATURES=("movie/home" "movie/detail" "cinema/schedule" "cinema/seatmap" "booking/checkout" "ticket/history" "ticket/detail")
for feat in "${FEATURES[@]}"; do
    CONTRACT_FILE=$(find shared/src/org/ariqhikari/movietix/presentation/$feat -name "*Contract.kt" 2>/dev/null || true)
    if [ -z "$CONTRACT_FILE" ]; then
        echo "  [FAIL] Missing Contract for feature: $feat"
        ERRORS=$((ERRORS + 1))
    else
        echo "  [PASS] Found Contract: $(basename "$CONTRACT_FILE")"
    fi
done

# 6. Audit Test Coverage & Targets
echo ""
echo "[6/6] Auditing Unit Test Coverage..."
TEST_COUNT=$(find shared/test -name "*Test.kt" | wc -l | tr -d ' ')
echo "  Found $TEST_COUNT unit test files in shared/test/."

echo ""
echo "=================================================="
echo "Audit Summary: $ERRORS errors, $WARNINGS warnings."
echo "=================================================="

if [ $ERRORS -gt 0 ]; then
    exit 1
fi
