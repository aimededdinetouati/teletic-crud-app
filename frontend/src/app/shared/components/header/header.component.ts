import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../auth/services/auth.service';
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css'],
    imports: [
        CommonModule
    ]
})
export class HeaderComponent implements OnInit {
    currentUser: any = null;
    isAdmin = false;

    constructor(private authService: AuthService, private router: Router) { }

    ngOnInit(): void {
        // Subscribe to current user changes
        this.authService.currentUser$.subscribe(user => {
            this.currentUser = user;
            this.isAdmin = this.authService.isAdmin();
        });
    }

    logout(): void {
        this.authService.logout();
    }
}