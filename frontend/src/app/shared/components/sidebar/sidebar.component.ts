import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../auth/services/auth.service';
import {RouterLink} from "@angular/router";
import {CommonModule} from "@angular/common";

interface NavItem {
    title: string;
    icon: string;
    link: string;
    adminOnly?: boolean;
}

@Component({
    selector: 'app-sidebar',
    templateUrl: './sidebar.component.html',
    imports: [
        RouterLink,
        CommonModule
    ],
    styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
    isAdmin = false;
    navItems: NavItem[] = [
        {
            title: 'Dashboard',
            icon: 'fa-tachometer-alt',
            link: '/admin/dashboard',
            adminOnly: true
        },
        {
            title: 'My Dashboard',
            icon: 'fa-tachometer-alt',
            link: '/user/dashboard',
            adminOnly: false
        },
        {
            title: 'Users',
            icon: 'fa-users',
            link: '/admin/users',
            adminOnly: true
        },
        {
            title: 'Tasks',
            icon: 'fa-tasks',
            link: '/admin/tasks',
            adminOnly: true
        },
        {
            title: 'My Tasks',
            icon: 'fa-clipboard-list',
            link: '/user/tasks',
            adminOnly: false
        }
    ];

    constructor(private authService: AuthService) { }

    ngOnInit(): void {
        this.isAdmin = this.authService.isAdmin();
    }

    getFilteredNavItems(): NavItem[] {
        if (this.isAdmin) {
            return this.navItems.filter(item => item.adminOnly || item.title === 'My Tasks');
        } else {
            return this.navItems.filter(item => !item.adminOnly);
        }
    }
}