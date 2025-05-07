import { Routes } from '@angular/router';
import { AuthGuard } from './auth/guards/auth.guard';
import { AdminGuard } from './auth/guards/admin.guard';

export const routes: Routes = [
    // Redirect empty path to login
    { path: '', redirectTo: '/auth/login', pathMatch: 'full' },

    // Auth module routes (public)
    {
        path: 'auth',
        loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule)
    },

    // Admin module routes (protected, admin only)
    {
        path: 'admin',
        loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule),
        canActivate: [AuthGuard, AdminGuard]
    },

    // User module routes (protected)
    {
        path: 'user',
        loadChildren: () => import('./user/user.module').then(m => m.UserModule),
        canActivate: [AuthGuard]
    },

    // Fallback route for 404
    { path: '**', redirectTo: '/auth/login' }
];