import { Component } from '@angular/core';
import {SidebarComponent} from "../sidebar/sidebar.component";
import {HeaderComponent} from "../header/header.component";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-main-layout',
    templateUrl: './main-layout.component.html',
    imports: [
        SidebarComponent,
        HeaderComponent,
        CommonModule
    ],
    styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent {
    // This component is just a container for our layout structure
}