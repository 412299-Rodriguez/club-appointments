# Club Los Amigos - Frontend

Angular 19+ application for the Club Los Amigos training session management system.

## Features

- **Modern Angular 19+**: Standalone components, signals, defer, @if, @for
- **Tailwind CSS**: Utility-first styling with custom color palette
- **Responsive Design**: Mobile-first approach
- **PWA Support**: Progressive Web App with service worker
- **Authentication**: JWT-based authentication with interceptors
- **Reactive Forms**: Form validation and management
- **Route Guards**: Protected routes based on authentication and roles

## Prerequisites

- Node.js 18+ and npm
- Angular CLI 19+

## Installation

1. Install dependencies:
```bash
npm install
```

2. If angular.json.new exists, replace angular.json:
```bash
cp angular.json.new angular.json
```

## Development

Start the development server:
```bash
npm start
# or
ng serve
```

The application will be available at `http://localhost:4200/`

## Build

### Development Build
```bash
npm run build
```

### Production Build
```bash
npm run build:prod
# or
ng build --configuration production
```

The build artifacts will be stored in the `dist/` directory.

## Project Structure

```
Frontend/
├── src/
│   ├── app/
│   │   ├── core/                  # Core module
│   │   │   ├── models/            # Data models
│   │   │   ├── services/          # Services
│   │   │   ├── guards/            # Route guards
│   │   │   └── interceptors/      # HTTP interceptors
│   │   ├── features/              # Feature modules
│   │   │   ├── landing/           # Landing page
│   │   │   ├── auth/              # Authentication (login, register)
│   │   │   ├── turnero/           # Training sessions
│   │   │   └── profile/           # User profile
│   │   ├── shared/                # Shared components
│   │   │   └── components/        # Reusable components
│   │   ├── app.component.ts       # Root component
│   │   ├── app.config.ts          # App configuration
│   │   └── app.routes.ts          # Routes
│   ├── assets/                    # Static assets
│   ├── environments/              # Environment configs
│   ├── styles.css                 # Global styles
│   ├── main.ts                    # Bootstrap file
│   └── index.html                 # HTML entry point
├── angular.json                   # Angular configuration
├── tailwind.config.js             # Tailwind configuration
├── tsconfig.json                  # TypeScript configuration
└── package.json                   # Dependencies

```

## Color Palette

The application uses the following color scheme:

- **Primary Background**: `#1a1f37`
- **Secondary Background**: `#252d4a`
- **Accent Blue**: `#4169e1`
- **Accent Red**: `#dc143c`
- **Card Background**: `#2d3555`
- **Input Background**: `#384160`
- **Border Color**: `#3f4a6b`
- **Text Primary**: `#ffffff`
- **Text Secondary**: `#9ca3af`

## API Integration

The application connects to the Spring Boot backend at `/api`. Update the API URL in:
- `src/environments/environment.ts` (development)
- `src/environments/environment.prod.ts` (production)

## PWA Icons

Generate PWA icons and place them in `src/assets/icons/`:
- icon-72x72.png
- icon-96x96.png
- icon-128x128.png
- icon-144x144.png
- icon-152x152.png
- icon-192x192.png
- icon-384x384.png
- icon-512x512.png

You can use tools like [PWA Builder](https://www.pwabuilder.com/) or [RealFaviconGenerator](https://realfavicongenerator.net/) to generate these icons.

## Docker

Build and run with Docker:

```bash
# Build
docker build -t club-amigos-frontend .

# Run
docker run -p 80:80 club-amigos-frontend
```

## License

Private project for Club Los Amigos
