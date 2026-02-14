document.addEventListener('DOMContentLoaded', () => {
  const noBtn = document.getElementById('noBtn');
  const yesBtn = document.getElementById('yesBtn');
  const canvas = document.getElementById('spriteCanvas');
  const ctx = canvas.getContext('2d');
  const questionBox = document.querySelector('.question-box');
  const successMessage = document.getElementById('successMessage');
  const envelope = document.getElementById('envelope');
  const letterContainer = document.getElementById('letterContainer');
  const closeLetter = document.getElementById('closeLetter');

  // Sprite configuration
  const SPRITE_SIZE = 120;
  canvas.width = SPRITE_SIZE;
  canvas.height = SPRITE_SIZE;

  // Load the face images
  const faceImg = new Image();
  faceImg.src = 'face.png';

  const kissingImg = new Image();
  kissingImg.src = 'kissing.png';

  let currentFaceImg = faceImg;

  // Animation states
  const STATES = {
    IDLE: 'idle',
    WALK: 'walk',
    PUSH: 'push'
  };

  let currentState = STATES.IDLE;
  let animationFrame = 0;
  let animationTimer = 0;
  let noBtnPos = { x: 0, y: 0 };
  let isPushing = false;
  let spritePos = { x: 0, y: 0 };
  let imageLoaded = false;
  let yesBtnScale = 1;
  let pushCount = 0;
  let pushDirection = { x: -1, y: 0 }; // Default: pushing left
  let spriteVisible = false;
  let hasMovedOnce = false;
  let gameOver = false;

  faceImg.onload = () => {
    imageLoaded = true;
  };

  // Draw the sprite character with your face
  function drawSprite(frame, state) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;

    // Animation - simple breathing effect
    let faceScale = 1 + Math.sin(frame * 0.1) * 0.02;

    ctx.save();
    ctx.translate(centerX, centerY);

    // Left grabbing hand
    ctx.save();
    ctx.translate(-50, 0);
    ctx.font = '40px Arial';
    ctx.fillText('🤚', -20, 20);
    ctx.restore();

    // Draw your actual face (circular pfp style)
    if (imageLoaded) {
      ctx.save();
      ctx.scale(faceScale, faceScale);

      const faceSize = 70;
      ctx.beginPath();
      ctx.arc(0, 0, faceSize / 2, 0, Math.PI * 2);
      ctx.clip();

      ctx.drawImage(currentFaceImg, -faceSize / 2, -faceSize / 2, faceSize, faceSize);
      ctx.restore();

      // Face border (pixel art style)
      ctx.strokeStyle = '#2d3436';
      ctx.lineWidth = 4;
      ctx.beginPath();
      ctx.arc(0, 0, 35, 0, Math.PI * 2);
      ctx.stroke();
    }

    // Right grabbing hand
    ctx.save();
    ctx.translate(50, 0);
    ctx.font = '40px Arial';
    ctx.fillText('🤚', -20, 20);
    ctx.restore();

    ctx.restore();
  }

  // Animation loop
  function animate() {
    animationTimer++;

    if (animationTimer % 2 === 0) {
      animationFrame++;
      drawSprite(animationFrame, currentState);
    }

    requestAnimationFrame(animate);
  }

  // Position sprite to follow and grab NO button
  function positionSprite() {
    const btnRect = noBtn.getBoundingClientRect();
    const btnCenterX = btnRect.left + btnRect.width / 2;
    const btnCenterY = btnRect.top + btnRect.height / 2;

    // Position sprite centered on the NO button
    spritePos.x = btnCenterX - SPRITE_SIZE / 2;
    spritePos.y = btnCenterY - SPRITE_SIZE / 2;

    canvas.style.left = spritePos.x + 'px';
    canvas.style.top = spritePos.y + 'px';

    // Show sprite visibility
    canvas.style.opacity = spriteVisible ? '1' : '0';
  }

  // Get distance between two points
  function getDistance(x1, y1, x2, y2) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
  }

  // Initialize NO button position
  function initNoButton() {
    // Don't change position initially, let it flow naturally
  }

  // Move NO button in all directions with contact effect
  function moveNoButton(mouseX, mouseY) {
    if (isPushing) return;

    // Calculate direction away from mouse
    const btnRect = noBtn.getBoundingClientRect();

    // On first move, switch to fixed positioning at current location
    if (!hasMovedOnce) {
      noBtnPos = {
        x: btnRect.left,
        y: btnRect.top
      };
      noBtn.style.position = 'fixed';
      noBtn.style.left = noBtnPos.x + 'px';
      noBtn.style.top = noBtnPos.y + 'px';
      hasMovedOnce = true;

      // Small delay to let fixed positioning settle before moving
      setTimeout(() => {
        moveNoButton(mouseX, mouseY);
      }, 50);
      return;
    }

    isPushing = true;
    currentState = STATES.PUSH;
    const btnCenterX = btnRect.left + btnRect.width / 2;
    const btnCenterY = btnRect.top + btnRect.height / 2;

    const deltaX = btnCenterX - mouseX;
    const deltaY = btnCenterY - mouseY;
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

    // Store normalized push direction for sprite positioning
    pushDirection = {
      x: deltaX / distance,
      y: deltaY / distance
    };

    // Normalize and push in that direction
    // Add some randomness to make it unpredictable
    const pushDistance = 200 + Math.random() * 50; // Random 200-250px
    let moveX = pushDirection.x * pushDistance;
    let moveY = pushDirection.y * pushDistance;

    // Calculate new position
    let newX = noBtnPos.x + moveX;
    let newY = noBtnPos.y + moveY;

    // Screen boundaries with larger padding to avoid corners
    const padding = 60;
    const maxX = window.innerWidth - btnRect.width - padding;
    const maxY = window.innerHeight - btnRect.height - padding;

    // Check if button would be trapped near edges
    const wouldHitLeft = newX < padding;
    const wouldHitRight = newX > maxX;
    const wouldHitTop = newY < padding;
    const wouldHitBottom = newY > maxY;

    // If trapped in corner or against edge, move toward center instead
    if ((wouldHitLeft || wouldHitRight) && (wouldHitTop || wouldHitBottom)) {
      // Corner trap - move to center area
      const centerX = window.innerWidth / 2;
      const centerY = window.innerHeight / 2;
      newX = centerX - btnRect.width / 2 + (Math.random() - 0.5) * 200;
      newY = centerY - btnRect.height / 2 + (Math.random() - 0.5) * 200;
    } else if (wouldHitLeft || wouldHitRight) {
      // Hitting left/right edge - move perpendicular (up or down)
      newX = wouldHitLeft ? padding : maxX;
      // Add extra vertical movement to escape
      newY = noBtnPos.y + (pushDirection.y * pushDistance * 2);
    } else if (wouldHitTop || wouldHitBottom) {
      // Hitting top/bottom edge - move perpendicular (left or right)
      newY = wouldHitTop ? padding : maxY;
      // Add extra horizontal movement to escape
      newX = noBtnPos.x + (pushDirection.x * pushDistance * 2);
    }

    // Final bounds check
    noBtnPos.x = Math.max(padding, Math.min(newX, maxX));
    noBtnPos.y = Math.max(padding, Math.min(newY, maxY));

    // Apply position (CSS transition will handle smooth movement)
    noBtn.style.left = noBtnPos.x + 'px';
    noBtn.style.top = noBtnPos.y + 'px';

    // Grow YES button
    pushCount++;
    yesBtnScale = 1 + (pushCount * 0.15);
    yesBtn.style.transform = `translate(2px, 2px) scale(${yesBtnScale})`;
    yesBtn.style.transition = 'transform 0.3s ease';

    // Return to idle after animation completes
    setTimeout(() => {
      currentState = STATES.IDLE;
      isPushing = false;
    }, 350); // Match CSS transition duration
  }

  // Check mouse proximity to NO button
  function checkProximity(e) {
    if (gameOver) return;

    // Always update sprite position to follow NO button
    positionSprite();

    if (isPushing) return;

    const btnRect = noBtn.getBoundingClientRect();
    const btnCenterX = btnRect.left + btnRect.width / 2;
    const btnCenterY = btnRect.top + btnRect.height / 2;

    const distance = getDistance(e.clientX, e.clientY, btnCenterX, btnCenterY);

    // Start with smaller trigger distance, increase after first move
    const triggerDistance = hasMovedOnce ? 200 : 120;

    if (distance < triggerDistance) {
      // Show sprite on first interaction
      spriteVisible = true;
      moveNoButton(e.clientX, e.clientY);
    }
  }

  // Handle YES button click
  yesBtn.addEventListener('click', () => {
    gameOver = true;
    spriteVisible = true;

    // Switch to kissing face
    currentFaceImg = kissingImg;

    // Show sprite with kissing face at fixed position (below panel)
    canvas.style.display = 'block';
    canvas.style.position = 'fixed';
    canvas.style.left = '50%';
    canvas.style.top = '65%';
    canvas.style.transform = 'translateX(-50%)';
    canvas.style.transition = 'none'; // Stop smooth transitions
    canvas.style.opacity = '1'; // Force visible
    canvas.style.zIndex = '1000';

    questionBox.style.display = 'none';
    successMessage.classList.add('show');

    // Show envelope in the sprite's hand after a delay
    setTimeout(() => {
      envelope.style.display = 'block';
      // Position envelope in the right hand of the sprite
      const canvasRect = canvas.getBoundingClientRect();
      envelope.style.left = (canvasRect.left + canvasRect.width / 2 + 30) + 'px';
      envelope.style.top = (canvasRect.top + canvasRect.height / 2 - 10) + 'px';
    }, 1000);

    // Create fireworks
    createFireworks();
  });

  // Handle envelope click
  envelope.addEventListener('click', () => {
    letterContainer.classList.add('show');
  });

  // Handle letter close
  closeLetter.addEventListener('click', () => {
    letterContainer.classList.remove('show');
  });

  // Close letter when clicking outside
  letterContainer.addEventListener('click', (e) => {
    if (e.target === letterContainer) {
      letterContainer.classList.remove('show');
    }
  });

  // Handle NO button click
  noBtn.addEventListener('click', (e) => {
    moveNoButton(e.clientX, e.clientY);
  });

  // Mouse move listener
  document.addEventListener('mousemove', checkProximity);

  // Fireworks effect
  function createFireworks() {
    const colors = ['#ff6b9d', '#ffd700', '#00b894', '#ff6b6b', '#a29bfe', '#fd79a8'];
    const emojis = ['💕', '💗', '💖', '💝', '❤️', '💘', '✨', '🎆', '🎇'];

    // Create multiple firework bursts
    for (let burst = 0; burst < 8; burst++) {
      setTimeout(() => {
        const burstX = 20 + Math.random() * 60; // Random position across screen
        const burstY = 20 + Math.random() * 40;

        // Create particles for each burst
        for (let i = 0; i < 15; i++) {
          const particle = document.createElement('div');

          // Mix emojis and colored particles
          if (Math.random() > 0.5) {
            particle.textContent = emojis[Math.floor(Math.random() * emojis.length)];
            particle.style.fontSize = '1.5rem';
          } else {
            particle.style.width = '8px';
            particle.style.height = '8px';
            particle.style.borderRadius = '50%';
            particle.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
          }

          particle.style.position = 'fixed';
          particle.style.left = burstX + '%';
          particle.style.top = burstY + '%';
          particle.style.pointerEvents = 'none';
          particle.style.zIndex = '999';

          const angle = (Math.PI * 2 * i) / 15;
          const velocity = 100 + Math.random() * 100;
          const tx = Math.cos(angle) * velocity;
          const ty = Math.sin(angle) * velocity;

          particle.style.animation = `firework-${burst}-${i} 1.5s ease-out forwards`;

          const keyframes = `
            @keyframes firework-${burst}-${i} {
              0% {
                transform: translate(0, 0);
                opacity: 1;
              }
              100% {
                transform: translate(${tx}px, ${ty + 200}px);
                opacity: 0;
              }
            }
          `;

          const styleSheet = document.createElement('style');
          styleSheet.textContent = keyframes;
          document.head.appendChild(styleSheet);

          document.body.appendChild(particle);

          setTimeout(() => {
            particle.remove();
            styleSheet.remove();
          }, 1500);
        }
      }, burst * 400);
    }
  }

  // Add fall animation for confetti
  const style = document.createElement('style');
  style.textContent = `
    @keyframes fall {
      to {
        transform: translateY(100vh) rotate(360deg);
        opacity: 0;
      }
    }
  `;
  document.head.appendChild(style);

  // Initialize
  initNoButton();
  animate();
});
