// script.js
// Robust sprite controller + "push NO button away" interaction.
//
// Assumes paree.png is a sprite sheet arranged in rows (animations) and columns (frames).
// Rows (top->bottom) per your notes: 0=waving, 1=brace, 2=pushing, 3=still.  (see filecite in chat)
//
// IMPORTANT HTML IDs required:
// <div class="game-container" id="game">
//   <div class="character" id="char"></div>
//   ...
//   <button id="noBtn">NO</button>

document.addEventListener("DOMContentLoaded", () => {
  const game = document.getElementById("game");
  const char = document.getElementById("char");
  const noBtn = document.getElementById("noBtn");

  if (!game || !char || !noBtn) {
    console.error("Missing required elements. Need #game, #char, #noBtn.");
    return;
  }

  // ---------------------------
  // CONFIG (adjust if needed)
  // ---------------------------
  const SPRITE_SHEET_PATH = "./paree.png";

  // How many frames exist across the sheet (columns) and how many animation rows.
  // If your sheet is NOT 4x4, change these.
  const SHEET_COLS = 4;
  const SHEET_ROWS = 4;

  // How many frames to animate for EACH row.
  // Setting PUSHING to 1 removes the “carousel” (no frame-advancing).
  // If you confirm pushing has 4 frames, change PUSHING to 4.
  const ROW_FRAMES = {
    0: 4, // WAVING
    1: 4, // BRACE
    2: 1, // PUSHING (default: 1 frame to avoid carousel)
    3: 1, // STILL
  };

  // Animation speed for rows that DO animate
  const FPS = 10;

  // Push behavior tuning
  const TRIGGER_DISTANCE = 130; // cursor distance to NO before we react
  const PUSH_DISTANCE = 170;    // how far NO moves per push
  const PUSH_TIME_MS = 260;     // duration to show pushing pose
  const PAD = 10;              // keep NO inside container padding
  // ---------------------------

  const ROW = {
    WAVING: 0,
    BRACE: 1,
    PUSHING: 2,
    STILL: 3,
  };

  const SPRITE = {
    cols: SHEET_COLS,
    rows: SHEET_ROWS,
    fps: FPS,
    frame: 0,
    row: ROW.STILL,
    frameW: 0,
    frameH: 0,
    timer: null,
    loaded: false,
  };

  const sheet = new Image();
  sheet.src = SPRITE_SHEET_PATH;

  function clamp(v, min, max) {
    return Math.max(min, Math.min(max, v));
  }

  function getRectCenter(rect) {
    return { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 };
  }

  function stopSprite() {
    if (SPRITE.timer) {
      clearInterval(SPRITE.timer);
      SPRITE.timer = null;
    }
  }

  function renderSpriteFrame() {
    const x = -(SPRITE.frame * SPRITE.frameW);
    const y = -(SPRITE.row * SPRITE.frameH);
    char.style.backgroundPosition = `${x}px ${y}px`;
  }

  function startSprite() {
    stopSprite();

    const framesThisRow = ROW_FRAMES[SPRITE.row] ?? SPRITE.cols;

    // If the row only has 1 frame, do not animate.
    if (framesThisRow <= 1) {
      SPRITE.frame = 0;
      renderSpriteFrame();
      return;
    }

    SPRITE.timer = setInterval(() => {
      SPRITE.frame = (SPRITE.frame + 1) % framesThisRow;
      renderSpriteFrame();
    }, 1000 / SPRITE.fps);
  }

  /**
   * Set sprite row. Auto-animates only if ROW_FRAMES[row] > 1.
   */
  function setSpriteRow(row) {
    SPRITE.row = row;
    SPRITE.frame = 0;
    renderSpriteFrame();
    startSprite();
  }

  sheet.onload = () => {
    SPRITE.frameW = Math.floor(sheet.width / SPRITE.cols);
    SPRITE.frameH = Math.floor(sheet.height / SPRITE.rows);

    char.style.width = `${SPRITE.frameW}px`;
    char.style.height = `${SPRITE.frameH}px`;
    char.style.backgroundImage = `url("${SPRITE_SHEET_PATH}")`;
    char.style.backgroundRepeat = "no-repeat";
    char.style.backgroundSize = `${sheet.width}px ${sheet.height}px`;
    char.style.imageRendering = "pixelated";

    // Start standing still, truly not animated (ROW_FRAMES[STILL]=1 handles it)
    SPRITE.loaded = true;
    setSpriteRow(ROW.STILL);
  };

  sheet.onerror = () => {
    console.error(`Could not load ${SPRITE_SHEET_PATH}. Check file name/path.`);
  };

  // ---------------------------
  // NO button push logic
  // ---------------------------
  let isPushing = false;
  let noOffset = { x: 0, y: 0 };

  function moveNoButtonBy(dx, dy) {
    const gameRect = game.getBoundingClientRect();
    const btnRect = noBtn.getBoundingClientRect();

    // Proposed absolute position
    const proposedLeftAbs = btnRect.left + dx;
    const proposedTopAbs = btnRect.top + dy;

    // Clamp within game container
    const minLeftAbs = gameRect.left + PAD;
    const maxLeftAbs = gameRect.right - btnRect.width - PAD;
    const minTopAbs = gameRect.top + PAD;
    const maxTopAbs = gameRect.bottom - btnRect.height - PAD;

    const clampedLeftAbs = clamp(proposedLeftAbs, minLeftAbs, maxLeftAbs);
    const clampedTopAbs = clamp(proposedTopAbs, minTopAbs, maxTopAbs);

    const appliedDx = clampedLeftAbs - btnRect.left;
    const appliedDy = clampedTopAbs - btnRect.top;

    noOffset.x += appliedDx;
    noOffset.y += appliedDy;

    noBtn.style.left = `${noOffset.x}px`;
    noBtn.style.top = `${noOffset.y}px`;
  }

  function placeCharNextToButton(pushDir) {
    const btnRect = noBtn.getBoundingClientRect();
    const btnCenter = getRectCenter(btnRect);
    const gameRect = game.getBoundingClientRect();

    // Stand “behind” button relative to push direction
    const standDistance = SPRITE.loaded ? SPRITE.frameW * 0.55 : 70;
    const standX = btnCenter.x - pushDir.x * standDistance;
    const standY = btnCenter.y - pushDir.y * standDistance;

    const localX = standX - gameRect.left;
    const localY = standY - gameRect.top;

    char.style.left = `${clamp(localX, 0, gameRect.width)}px`;
    char.style.top = `${clamp(localY, 0, gameRect.height)}px`;
    char.style.transform = "translate(-50%, -50%)";
  }

  function pushNoAwayFrom(mouseX, mouseY) {
    if (!SPRITE.loaded || isPushing) return;
    isPushing = true;

    const btnRect = noBtn.getBoundingClientRect();
    const btnCenter = getRectCenter(btnRect);

    // Unit vector pointing away from mouse
    let dx = btnCenter.x - mouseX;
    let dy = btnCenter.y - mouseY;
    const len = Math.hypot(dx, dy) || 1;
    dx /= len;
    dy /= len;

    const pushDir = { x: dx, y: dy };

    placeCharNextToButton(pushDir);

    // Show pushing pose (animates ONLY if ROW_FRAMES[2] > 1)
    setSpriteRow(ROW.PUSHING);

    // Move the button away
    moveNoButtonBy(pushDir.x * PUSH_DISTANCE, pushDir.y * PUSH_DISTANCE);

    // Return to still pose (no animation)
    window.setTimeout(() => {
      setSpriteRow(ROW.STILL);
      isPushing = false;
    }, PUSH_TIME_MS);
  }

  game.addEventListener("mousemove", (e) => {
    const btnRect = noBtn.getBoundingClientRect();
    const c = getRectCenter(btnRect);
    const dist = Math.hypot(c.x - e.clientX, c.y - e.clientY);

    if (dist < TRIGGER_DISTANCE) {
      pushNoAwayFrom(e.clientX, e.clientY);
    }
  });
});
