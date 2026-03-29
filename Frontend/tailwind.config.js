/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        dark:          '#0D1117',
        surface:       '#161B22',
        surfaceHover:  '#21262D',
        surfaceBorder: '#30363D',
        primary:       '#A78BFA',       // violet-400
        primaryHover:  '#8B5CF6',       // violet-500
        primaryGlow:   'rgba(139,92,246,0.3)',
        secondary:     '#34D399',       // emerald-400
        secondaryHover:'#10B981',
        accent:        '#FBBF24',       // amber-400
        error:         '#F87171',       // red-400
        muted:         '#8B949E',
        mutedLight:    '#C9D1D9',
        charcoal:      '#2D3748',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'hero-gradient':   'linear-gradient(135deg, #0D1117 0%, #1a103a 50%, #0D1117 100%)',
        'card-gradient':   'linear-gradient(135deg, rgba(167,139,250,0.08) 0%, rgba(52,211,153,0.04) 100%)',
        'glow-gradient':   'radial-gradient(ellipse at center, rgba(139,92,246,0.15) 0%, transparent 70%)',
      },
      boxShadow: {
        'glow-primary': '0 0 20px rgba(139,92,246,0.3)',
        'glow-secondary': '0 0 20px rgba(52,211,153,0.2)',
        'card-shadow': '0 4px 24px rgba(0,0,0,0.4)',
        'modal-shadow': '0 25px 50px rgba(0,0,0,0.7)',
      },
      animation: {
        'fade-in':    'fadeIn 0.3s ease-out',
        'slide-up':   'slideUp 0.3s ease-out',
        'slide-in':   'slideIn 0.3s ease-out',
        'pulse-glow': 'pulseGlow 2s ease-in-out infinite',
        'spin-slow':  'spin 3s linear infinite',
      },
      keyframes: {
        fadeIn: {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%':   { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideIn: {
          '0%':   { opacity: '0', transform: 'translateX(-16px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        pulseGlow: {
          '0%, 100%': { boxShadow: '0 0 10px rgba(139,92,246,0.2)' },
          '50%':       { boxShadow: '0 0 30px rgba(139,92,246,0.5)' },
        },
      },
    },
  },
  plugins: [],
}
